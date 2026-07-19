package dev.gatotauth.api.domain.account;

import dev.gatotauth.api.domain.session.Session;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Account represents the central aggregate root of the GatotAuth domain (Option D).
 * <p>
 * API Status: Stable
 */
public final class Account {
    private final AccountId id;
    private final List<Identity> identities;
    private final List<Credential> credentials;
    private final List<Session> sessions;
    private final List<Device> devices;
    private final SecurityPolicy securityPolicy;
    private final Map<String, String> metadata;
    private final Instant createdAt;
    private final boolean locked;

    /**
     * Instantiates an Account aggregate.
     */
    public Account(
            AccountId id,
            List<Identity> identities,
            List<Credential> credentials,
            List<Session> sessions,
            List<Device> devices,
            SecurityPolicy securityPolicy,
            Map<String, String> metadata,
            Instant createdAt,
            boolean locked
    ) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.identities = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(identities, "identities cannot be null")));
        this.credentials = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(credentials, "credentials cannot be null")));
        this.sessions = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(sessions, "sessions cannot be null")));
        this.devices = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(devices, "devices cannot be null")));
        this.securityPolicy = Objects.requireNonNull(securityPolicy, "securityPolicy cannot be null");
        this.metadata = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(metadata, "metadata cannot be null")));
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.locked = locked;
    }

    public AccountId id() {
        return id;
    }

    public List<Identity> identities() {
        return identities;
    }

    public List<Credential> credentials() {
        return credentials;
    }

    public List<Session> sessions() {
        return sessions;
    }

    public List<Device> devices() {
        return devices;
    }

    public SecurityPolicy securityPolicy() {
        return securityPolicy;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * Resolves an identity by its specific implementation class.
     *
     * @param clazz Target identity subtype.
     * @param <T> Identity subclass.
     * @return Optional match.
     */
    @SuppressWarnings("unchecked")
    public <T extends Identity> Optional<T> findIdentity(Class<T> clazz) {
        return identities.stream()
                .filter(clazz::isInstance)
                .map(i -> (T) i)
                .findFirst();
    }

    /**
     * Resolves a credential by its specific implementation class.
     *
     * @param clazz Target credential subtype.
     * @param <T> Credential subclass.
     * @return Optional match.
     */
    @SuppressWarnings("unchecked")
    public <T extends Credential> Optional<T> findCredential(Class<T> clazz) {
        return credentials.stream()
                .filter(clazz::isInstance)
                .map(c -> (T) c)
                .findFirst();
    }
}
