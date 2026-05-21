package com.shop.security.impl;

import com.shop.security.PasswordBreachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * HIBP k-anonymity implementation: computes the SHA-1 hash of the password, sends only the
 * first 5 hex characters to {@code https://api.pwnedpasswords.com/range/{prefix}},
 * then checks whether the suffix appears in the response (SEC-PWD-002 / CPA-16).
 */
@Service
public class PasswordBreachServiceImpl implements PasswordBreachService {

    private static final Logger log = LoggerFactory.getLogger(PasswordBreachServiceImpl.class);
    private static final String HIBP_URL = "https://api.pwnedpasswords.com/range/";

    /** {@inheritDoc} */
    @Override
    public boolean isCompromised(String plainPassword) {
        try {
            String sha1 = sha1Hex(plainPassword).toUpperCase();
            String prefix = sha1.substring(0, 5);
            String suffix = sha1.substring(5);

            HttpURLConnection conn = (HttpURLConnection) new URL(HIBP_URL + prefix).openConnection();
            conn.setRequestProperty("Add-Padding", "true");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(line -> line.split(":")[0])
                        .anyMatch(suffix::equals);
            }
        } catch (Exception e) {
            // Fail open: a network failure must not block registration/password change.
            log.warn("HIBP check failed — allowing password ({})", e.getMessage());
            return false;
        }
    }

    private static String sha1Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
