package dev.gatotauth.core.infra.storage.sql;

import dev.gatotauth.api.domain.account.Account;
import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.account.ApiTokenCredential;
import dev.gatotauth.api.domain.account.Credential;
import dev.gatotauth.api.domain.account.FloodgateIdentity;
import dev.gatotauth.api.domain.account.Identity;
import dev.gatotauth.api.domain.account.MojangIdentity;
import dev.gatotauth.api.domain.account.OfflineIdentity;
import dev.gatotauth.api.domain.account.PasswordCredential;
import dev.gatotauth.api.domain.account.SecurityPolicy;
import dev.gatotauth.api.domain.account.TotpCredential;
import dev.gatotauth.api.domain.account.WebAuthnCredential;
import dev.gatotauth.api.provider.AccountRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * SqlAccountRepository persists Account aggregates inside an SQL database.
 */
public final class SqlAccountRepository implements AccountRepository {
    private final DatabaseConnector connector;
    private final Executor ioExecutor;

    /**
     * Instantiates SqlAccountRepository.
     */
    public SqlAccountRepository(DatabaseConnector connector, Executor ioExecutor) {
        this.connector = connector;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public CompletableFuture<Optional<Account>> findById(AccountId id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "SELECT created_at, locked FROM gatotauth_accounts WHERE id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, id.value().toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        Instant createdAt = Instant.parse(rs.getString("created_at"));
                        boolean locked = rs.getBoolean("locked");

                        List<Identity> identities = fetchIdentities(conn, id.value());
                        List<Credential> credentials = fetchCredentials(conn, id.value());

                        Account account = new Account(
                                id,
                                identities,
                                credentials,
                                List.of(),
                                List.of(),
                                new SecurityPolicy(8, 5, Duration.ofMinutes(15)),
                                Map.of(),
                                createdAt,
                                locked
                        );
                        return Optional.of(account);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error loading account by ID", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Optional<Account>> findByIdentity(String providerType, String identifier) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "SELECT account_id FROM gatotauth_identities "
                        + "WHERE provider_type = ? AND identifier = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, providerType);
                    ps.setString(2, identifier);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return Optional.<UUID>empty();
                        }
                        return Optional.of(UUID.fromString(rs.getString("account_id")));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error loading account by identity", e);
            }
        }, ioExecutor).thenCompose(optId -> {
            if (optId.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return findById(new AccountId(optId.get()));
        });
    }

    @Override
    public CompletableFuture<Void> save(Account account) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    String upsertAcc = "INSERT INTO gatotauth_accounts (id, created_at, locked) VALUES (?, ?, ?) "
                            + "ON CONFLICT(id) DO UPDATE SET locked = ?;";
                    try (PreparedStatement ps = conn.prepareStatement(upsertAcc)) {
                        ps.setString(1, account.id().value().toString());
                        ps.setString(2, account.createdAt().toString());
                        ps.setBoolean(3, account.isLocked());
                        ps.setBoolean(4, account.isLocked());
                        ps.executeUpdate();
                    }

                    String delIdentities = "DELETE FROM gatotauth_identities WHERE account_id = ?;";
                    try (PreparedStatement del = conn.prepareStatement(delIdentities)) {
                        del.setString(1, account.id().value().toString());
                        del.executeUpdate();
                    }
                    String insId = "INSERT INTO gatotauth_identities "
                            + "(account_id, provider_type, identifier, username) VALUES (?, ?, ?, ?);";
                    try (PreparedStatement ps = conn.prepareStatement(insId)) {
                        for (Identity identity : account.identities()) {
                            ps.setString(1, account.id().value().toString());
                            ps.setString(2, identity.getClass().getSimpleName());
                            ps.setString(3, identity.identifier());
                            ps.setString(4, getIdentityUsername(identity));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    String delCredentials = "DELETE FROM gatotauth_credentials WHERE account_id = ?;";
                    try (PreparedStatement del = conn.prepareStatement(delCredentials)) {
                        del.setString(1, account.id().value().toString());
                        del.executeUpdate();
                    }
                    String insCred = "INSERT INTO gatotauth_credentials "
                            + "(account_id, credential_type, credential_value) VALUES (?, ?, ?);";
                    try (PreparedStatement ps = conn.prepareStatement(insCred)) {
                        for (Credential credential : account.credentials()) {
                            ps.setString(1, account.id().value().toString());
                            ps.setString(2, credential.getClass().getSimpleName());
                            ps.setString(3, getCredentialValue(credential));
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error saving account to SQL database", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Void> delete(AccountId id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "DELETE FROM gatotauth_accounts WHERE id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, id.value().toString());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting account by ID", e);
            }
        }, ioExecutor);
    }

    private List<Identity> fetchIdentities(Connection conn, UUID accountId) throws SQLException {
        List<Identity> list = new ArrayList<>();
        String query = "SELECT provider_type, identifier, username FROM gatotauth_identities WHERE account_id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, accountId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("provider_type");
                    String identifier = rs.getString("identifier");
                    String username = rs.getString("username");
                    if ("MojangIdentity".equals(type)) {
                        list.add(new MojangIdentity(UUID.fromString(identifier), username));
                    } else if ("OfflineIdentity".equals(type)) {
                        list.add(new OfflineIdentity(accountId, username));
                    } else if ("FloodgateIdentity".equals(type)) {
                        list.add(new FloodgateIdentity(UUID.fromString(identifier), username));
                    }
                }
            }
        }
        return list;
    }

    private List<Credential> fetchCredentials(Connection conn, UUID accountId) throws SQLException {
        List<Credential> list = new ArrayList<>();
        String query = "SELECT credential_type, credential_value FROM gatotauth_credentials WHERE account_id = ?;";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, accountId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("credential_type");
                    String value = rs.getString("credential_value");
                    if ("PasswordCredential".equals(type)) {
                        list.add(new PasswordCredential(value));
                    } else if ("TotpCredential".equals(type)) {
                        list.add(new TotpCredential(value));
                    } else if ("ApiTokenCredential".equals(type)) {
                        list.add(new ApiTokenCredential(value));
                    } else if ("WebAuthnCredential".equals(type)) {
                        String[] split = value.split(":", 2);
                        list.add(new WebAuthnCredential(split[0], split[1]));
                    }
                }
            }
        }
        return list;
    }

    private String getIdentityUsername(Identity identity) {
        if (identity instanceof MojangIdentity mi) {
            return mi.username();
        } else if (identity instanceof OfflineIdentity oi) {
            return oi.username();
        } else if (identity instanceof FloodgateIdentity fi) {
            return fi.username();
        }
        return "";
    }

    private String getCredentialValue(Credential cred) {
        if (cred instanceof PasswordCredential pc) {
            return pc.hashedPassword();
        } else if (cred instanceof TotpCredential tc) {
            return tc.secretKey();
        } else if (cred instanceof ApiTokenCredential ac) {
            return ac.tokenHash();
        } else if (cred instanceof WebAuthnCredential wc) {
            return wc.credentialId() + ":" + wc.publicKey();
        }
        return "";
    }
}
