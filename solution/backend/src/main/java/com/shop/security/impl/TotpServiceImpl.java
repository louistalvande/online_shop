package com.shop.security.impl;

import com.shop.security.TotpService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * TOTP implementation backed by {@code dev.samstevens.totp}.
 * Uses SHA-1 / 6 digits / 30-second step with ±1 step tolerance (RFC 6238 / CPA-15).
 */
@Service
public class TotpServiceImpl implements TotpService {

    private static final int SECRET_BITS = 160;

    @Value("${app.mfa.issuer:OnlineShop}")
    private String issuer;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(SECRET_BITS);
    private final CodeVerifier codeVerifier;

    /** Constructs the service and initialises the TOTP verifier. */
    public TotpServiceImpl() {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        ((DefaultCodeVerifier) this.codeVerifier).setTimePeriod(30);
        ((DefaultCodeVerifier) this.codeVerifier).setAllowedTimePeriodDiscrepancy(1);
    }

    /** {@inheritDoc} */
    @Override
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /** {@inheritDoc} */
    @Override
    public String buildOtpauthUri(String secret, String email) {
        return "otpauth://totp/"
                + encode(issuer + ":" + email)
                + "?secret=" + secret
                + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=6&period=30";
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCodeValid(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }
}
