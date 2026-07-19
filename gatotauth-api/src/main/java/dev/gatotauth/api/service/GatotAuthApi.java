package dev.gatotauth.api.service;

/**
 * Access point to GatotAuth API capability sets.
 * <p>
 * API Status: Stable
 *
 * @since 1.0.0
 */
public interface GatotAuthApi {
    /**
     * Gets the static API instance.
     *
     * @return GatotAuthApi interface instance.
     */
    static GatotAuthApi get() {
        return GatotAuthApiProvider.get();
    }

    /**
     * Gets the Account manager service.
     *
     * @return AccountManager instance.
     */
    AccountManager getAccountManager();

    /**
     * Gets the Session manager service.
     *
     * @return SessionManager instance.
     */
    SessionManager getSessionManager();
}
