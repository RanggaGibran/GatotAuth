package dev.gatotauth.api.domain.account;

import java.util.Objects;

/**
 * Record representing connecting client platform OS and device footprint properties.
 * <p>
 * API Status: Stable
 *
 * @param osType The client platform operating system type name.
 * @param hardwareId Unique client hardware fingerprint identifier.
 * @since 1.0.0
 */
public record Device(String osType, String hardwareId) {
    /**
     * Validates Device parameters.
     */
    public Device {
        Objects.requireNonNull(osType, "osType cannot be null");
        Objects.requireNonNull(hardwareId, "hardwareId cannot be null");
    }
}
