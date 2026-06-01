# Skill : impl-us — Implémentation d'une User Story ARCADIA

Invoqué quand l'utilisateur dit **"next US"**, **"implémenter la prochaine US"**, **"impl US"** ou désigne une US spécifique (ex. "US-PRF-05").

---

## 1. Identifier la prochaine US

Grep `À faire` dans `architecture/05-backlog/us.adoc`.  
Si une US précise est demandée, aller directement à l'étape 2 avec celle-ci.  
Si plusieurs sont "À faire", vérifier si certaines sont déjà implémentées (explorer le code) avant de commencer.

---

## 2. Lire les critères d'acceptation

Lire le bloc de la US dans `us.adoc` (story + critères d'acceptation).  
Si les critères mentionnent une fonction ARCADIA (FS-*, CS-*, FS-S*…), lire la section correspondante dans `architecture/` avant de coder.

---

## 3. Créer la todo-list (TodoWrite)

Avant tout code, créer une todo-list avec **les étapes dans l'ordre** :

```
[ ] Backend : schéma SQL si nécessaire
[ ] Backend : entité / DTO / repository / service / controller
[ ] Tests unitaires : *ControllerImplTest + *ServiceImplTest (Mockito)
[ ] Run tests backend : ./mvnw test-compile puis ./mvnw test
[ ] Frontend : API client
[ ] Frontend : composant UI
[ ] i18n : fr / en / es
[ ] E2E : <domaine>/<us-id>.spec.js  ← OBLIGATOIRE, jamais oublié
[ ] Run E2E : npx playwright test e2e/<domaine>/<us-id>.spec.js
[ ] Marquer US Terminée dans us.adoc
```

**Les tests sont des critères de "done" — les mettre dans la todo-list dès le départ.**

---

## 4. Règles d'implémentation backend

### Ordre des fichiers

1. `V1__init_schema.sql` — colonnes nouvelles (pre-release : modifier V1, pas créer V2)
2. Entité JPA (`entity/`)
3. DTOs (`dto/`) — request + response séparés
4. Repository (`repository/`) — `JpaRepository` + `JpaSpecificationExecutor` si filtrage dynamique
5. Service interface (`service/`) puis impl (`service/impl/`)
6. Controller interface (`controller/`) puis impl (`controller/impl/`)

### Contraintes strictes (CLAUDE.md)

- Controller = interface + impl séparés. `@RestController` uniquement sur l'impl.
- Service = interface + impl séparés. `@Service @Transactional` uniquement sur l'impl.
- Jamais de `@Repository` injecté dans un Controller.
- Tous champs publics et méthodes publiques → Javadoc obligatoire.
- Tout le code en **anglais** (noms, commentaires, SQL, chemins API).
- Chaînes UI, emails, erreurs API → localisées FR/EN/ES dans `messages.properties`.

### Notifications email

Ajouter dans `NotificationService` (interface) + `NotificationServiceImpl` avec `@Async`.  
Clés i18n dans `messages.properties`, `messages_en.properties`, `messages_es.properties`.

### Job planifié

Utiliser `@Scheduled` sur une méthode de service.  
`@EnableScheduling` est sur `ShopApplication` — ne pas dupliquer.

---

## 5. Tests unitaires backend (Mockito) — OBLIGATOIRE

Écrire **après** les fichiers de production, **avant** de lancer les tests.

### Pattern controller (`*ControllerImplTest.java`)

```java
@ExtendWith(MockitoExtension.class)
class FooControllerImplTest {

    @Mock FooService fooService;
    @Mock MessageSource messageSource;
    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new FooControllerImpl(fooService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    @Test
    void getNominal_returns200() throws Exception {
        given(fooService.get(any())).willReturn(sampleResponse());
        mvc.perform(get("/api/admin/foos/{id}", UUID.randomUUID()))
           .andExpect(status().isOk());
    }

    @Test
    void getNotFound_returns404() throws Exception {
        given(fooService.get(any())).willThrow(new FooNotFoundException(UUID.randomUUID()));
        mvc.perform(get("/api/admin/foos/{id}", UUID.randomUUID()))
           .andExpect(status().isNotFound());
    }
}
```

### Pattern service (`*ServiceImplTest.java`)

```java
@ExtendWith(MockitoExtension.class)
class FooServiceImplTest {

    @Mock FooRepository fooRepository;
    FooServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FooServiceImpl(fooRepository);
    }

    @Test
    void create_savesAndReturnsResponse() {
        given(fooRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        FooResponse result = service.create(new CreateFooRequest("name"));
        assertThat(result.getName()).isEqualTo("name");
    }

    @Test
    void get_throwsWhenNotFound() {
        given(fooRepository.findById(any())).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(UUID.randomUUID()))
                .isInstanceOf(FooNotFoundException.class);
    }
}
```

**Couvrir** : chemin nominal + chaque exception métier du service + chaque code HTTP non-2xx du controller.

### Localisation des fichiers

`solution/backend/src/test/java/com/shop/<domaine>/controller/impl/<Nom>ControllerImplTest.java`  
`solution/backend/src/test/java/com/shop/<domaine>/service/impl/<Nom>ServiceImplTest.java`

---

## 6. Lancer les tests backend

Commandes à exécuter **dans le conteneur `shop-backend`** (SSH `shop-backend`) ou en local :

```bash
# Vérifier que le code compile (rapide)
./mvnw test-compile -q

# Lancer tous les tests unitaires
./mvnw test

# Lancer uniquement les tests d'un domaine
./mvnw test -Dtest="com.shop.account.*"

# Lancer un test précis
./mvnw test -Dtest="AccountServiceImplTest"
```

Si des tests échouent → corriger avant de continuer.  
Si la compilation échoue → corriger les imports/signatures avant tout.

---

## 7. Règles d'implémentation frontend

### Par SPA concernée

| SPA | Répertoire | Authentification |
|-----|-----------|-----------------|
| Buyer portal | `frontend/buyer-portal/` | `authedFetch` + `credentials: 'include'` |
| Vendor backoffice | `frontend/vendor-backoffice/` | `authedFetch` + `credentials: 'include'` |
| Admin console | `frontend/admin-console/` | `authedFetch` + `credentials: 'include'` |

**Ne jamais utiliser `Authorization: Bearer` dans les API clients frontend** — US-SEC-01 a supprimé ce pattern. Utiliser exclusivement `authedFetch` qui passe `credentials: 'include'`.

### i18n

Ajouter toutes les clés dans les 3 fichiers : `fr.json`, `en.json`, `es.json`.  
Aucune chaîne UI hardcodée — tout passe par `t('clé')`.

---

## 8. Tests E2E (Playwright JS) — OBLIGATOIRE

Fichier : `frontend/e2e/<domaine>/<us-id>.spec.js`

Exemples de nommage :
- `e2e/profile/prf-04.spec.js` (buyer portal)
- `e2e/account/sec-04.spec.js` (admin console)
- `e2e/profile/prf-05.spec.js` (vendor portal)

Enregistrer le spec dans `playwright.config.js` dans le projet (`buyer-portal`, `vendor-portal`, ou `admin-console`).

**Couvrir obligatoirement :**
- Chemin nominal (happy path)
- Cas d'erreur principaux définis dans les critères d'acceptation
- Contrôle d'accès (403 si rôle non autorisé)

**Helpers disponibles** dans `e2e/helpers/login.js` :
- `registerAndActivateBuyerViaApi`, `getBuyerToken`, `injectBuyerSession`
- `getAdminToken`, `loginAsAdmin`
- `createActiveVendorViaApi`, `getVendorToken`, `injectVendorSession`
- `createProductViaApi`, `createAddressViaApi`, `createCarrierViaApi`

---

## 9. Lancer les tests E2E

Commandes à exécuter depuis `solution/frontend/e2e/` :

```bash
# Un spec précis (le plus courant après une US)
npx playwright test e2e/<domaine>/<us-id>.spec.js

# Avec affichage détaillé
npx playwright test e2e/<domaine>/<us-id>.spec.js --reporter=list

# Tous les specs d'un projet
npx playwright test --project=buyer-portal

# Mode UI interactif (debug)
npx playwright test e2e/<domaine>/<us-id>.spec.js --ui
```

**Variables d'environnement** nécessaires si le stack n'écoute pas sur les ports par défaut :
```bash
API_URL=http://localhost:8080 \
BUYER_URL=http://buyer.localhost \
VENDOR_URL=http://vendor.localhost \
ADMIN_URL=http://admin.localhost \
npx playwright test ...
```

Si des tests E2E échouent → diagnostiquer (screenshots dans `test-results/`), corriger le code ou le test, re-lancer.

---

## 10. Marquer la US Terminée

Dans `architecture/05-backlog/us.adoc` :
- Cocher tous les `[ ]` → `[x]`
- Changer le statut `À faire` → `Terminée`

---

## 11. Checklist finale avant de clôturer

- [ ] `./mvnw test` passe sans erreur
- [ ] `npx playwright test e2e/<domaine>/<us-id>.spec.js` passe sans erreur
- [ ] Tous les `[ ]` cochés dans us.adoc
- [ ] Aucune chaîne UI hardcodée (pas de texte en dur dans le JSX)
- [ ] i18n : fr + en + es à jour
- [ ] E2E spec enregistré dans playwright.config.js
- [ ] Aucun `session.token` ni `authHeader()` dans les API clients frontend
- [ ] Javadoc sur tous les membres publics Java
- [ ] Schéma SQL dans V1 (pas V2) si pré-release
