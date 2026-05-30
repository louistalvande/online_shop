package com.shop.account.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for {@link TotpSecretConverter}. */
class TotpSecretConverterTest {

    // Base64("test-encryption-key-32-bytes-123") = 32 bytes exactly
    private static final String DEV_KEY = "dGVzdC1lbmNyeXB0aW9uLWtleS0zMi1ieXRlcy0xMjM=";

    TotpSecretConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TotpSecretConverter(DEV_KEY);
    }

    /** Encrypting then decrypting a plaintext yields the original value. */
    @Test
    void roundtrip_encryptThenDecrypt_returnsOriginal() {
        String plaintext = "JBSWY3DPEHPK3PXP";  // typical Base32 TOTP secret

        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    /** Two encryptions of the same value produce different ciphertexts (random IV). */
    @Test
    void encrypt_sameInput_producesDifferentCiphertexts() {
        String plaintext = "JBSWY3DPEHPK3PXP";

        String enc1 = converter.convertToDatabaseColumn(plaintext);
        String enc2 = converter.convertToDatabaseColumn(plaintext);

        assertThat(enc1).isNotEqualTo(enc2);
    }

    /** Null plaintext is stored as null (MFA not configured). */
    @Test
    void null_plaintext_storesAsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    /** Null stored value is returned as null. */
    @Test
    void null_storedValue_returnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    /** The stored value is Base64-encoded and not the plaintext. */
    @Test
    void storedValue_isNotPlaintext() {
        String plaintext = "JBSWY3DPEHPK3PXP";
        String stored = converter.convertToDatabaseColumn(plaintext);

        assertThat(stored).isNotEqualTo(plaintext);
        // Must be valid Base64
        assertThat(Base64.getDecoder().decode(stored)).hasSizeGreaterThan(12);
    }

    /** A 31-byte key (too short) causes an exception at construction time. */
    @Test
    void invalidKeyLength_throws() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[31]);
        assertThatThrownBy(() -> new TotpSecretConverter(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }
}
