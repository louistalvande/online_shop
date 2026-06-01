package com.shop.account.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA {@link AttributeConverter} that transparently encrypts and decrypts the {@code totp_secret}
 * column using AES-256-GCM (US-SEC-07 / FS-S08 / CS-16 / CPA-14..CPA-15).
 *
 * <p>Storage format: Base64( IV[12 bytes] || ciphertext )
 * A fresh random IV is generated for every write so repeated encryptions of the same secret
 * produce different ciphertexts.
 *
 * <p>The AES key is a 256-bit secret injected from the environment variable
 * {@code TOTP_ENCRYPTION_KEY} (Base64-encoded, 32 bytes decoded).
 * It must never appear in source code, logs, API responses, or Flyway migrations.
 */
@Converter
@Component
public class TotpSecretConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM    = "AES/GCM/NoPadding";
    private static final int    IV_BYTES     = 12;
    private static final int    GCM_TAG_BITS = 128;

    private final SecretKey secretKey;

    /**
     * @param base64Key Base64-encoded 32-byte AES-256 key injected from the environment
     *                  (property {@code app.totp.encryption-key} / env var {@code TOTP_ENCRYPTION_KEY})
     */
    public TotpSecretConverter(
            @Value("${app.totp.encryption-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "TOTP encryption key must be 32 bytes (256 bits) after Base64 decoding");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts a plaintext TOTP secret before writing it to the database column.
     * Returns {@code null} if the input is {@code null} (MFA not yet configured).
     *
     * @param plaintext the raw Base32 TOTP secret, or {@code null}
     * @return Base64-encoded {@code IV || ciphertext}, or {@code null}
     */
    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] combined = new byte[IV_BYTES + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_BYTES);
            System.arraycopy(ciphertext, 0, combined, IV_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt TOTP secret", e);
        }
    }

    /**
     * Decrypts a database column value back to the plaintext TOTP secret.
     * Returns {@code null} if the stored value is {@code null} (MFA not configured).
     *
     * @param stored Base64-encoded {@code IV || ciphertext} from the database, or {@code null}
     * @return the original plaintext TOTP secret, or {@code null}
     */
    @Override
    public String convertToEntityAttribute(String stored) {
        if (stored == null) return null;
        try {
            byte[] combined  = Base64.getDecoder().decode(stored);
            byte[] iv         = new byte[IV_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_BYTES);
            System.arraycopy(combined, IV_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt TOTP secret", e);
        }
    }
}
