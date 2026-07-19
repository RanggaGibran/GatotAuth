package dev.gatotauth.core.security;

import dev.gatotauth.api.provider.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

/**
 * BcryptPasswordHasher implements PasswordHasher using BCrypt.
 */
public final class BcryptPasswordHasher implements PasswordHasher {
    private final int logRounds;

    /**
     * Instantiates BcryptPasswordHasher with validation checks.
     */
    public BcryptPasswordHasher(int logRounds) {
        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("BCrypt strength (log rounds) must be between 4 and 31.");
        }
        this.logRounds = logRounds;
    }

    @Override
    public String algorithmName() {
        return "bcrypt";
    }

    @Override
    public String hash(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean verify(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
