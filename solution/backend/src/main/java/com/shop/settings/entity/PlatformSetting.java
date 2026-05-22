package com.shop.settings.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persistent key-value store for platform-wide configuration (US-ADM-10). */
@Entity
@Table(name = "platform_settings")
public class PlatformSetting {

    @Id
    @Schema(description = "Setting key")
    private String key;

    @Column(name = "value", nullable = false)
    @Schema(description = "Setting value as a string")
    private String value;

    /** Returns the setting key. */
    public String getKey() {
        return key;
    }

    /** Sets the setting key. */
    public void setKey(String key) {
        this.key = key;
    }

    /** Returns the setting value. */
    public String getValue() {
        return value;
    }

    /** Sets the setting value. */
    public void setValue(String value) {
        this.value = value;
    }
}
