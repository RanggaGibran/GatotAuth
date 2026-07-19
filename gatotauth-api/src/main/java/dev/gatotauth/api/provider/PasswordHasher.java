package dev.gatotauth.api.provider;

/**
 * Service provider interface for custom password hashing implementations.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface PasswordHasher {
    /**
     * Gets the unique name of the hashing algorithm (e.g. "argon2id", "bcrypt").
     *
     * @return Hashing algorithm identifier.
     */
    String algorithmName();

    /**
     * Cryptographically hashes a plain text password.
     *
     * @param plainTextPassword Raw user password.
     * @return Cryptographic hash string.
     */
    String hash(String plainTextPassword);

    /**
     * Verifies a plain text password against a stored hash string.
     *
     * @param plainTextPassword Raw password inputs.
     * @param hashedPassword Encoded hash outputs to check against.
     * @return true if credentials match.
     */
    boolean verify(String plainTextPassword, String hashedPassword);
}
