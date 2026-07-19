package dev.gatotauth.core.infra.storage.sql;

import dev.gatotauth.api.domain.account.AccountId;
import dev.gatotauth.api.domain.session.Session;
import dev.gatotauth.api.domain.session.SessionId;
import dev.gatotauth.api.provider.SessionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * SqlSessionRepository persists player sessions inside an SQL database.
 */
public final class SqlSessionRepository implements SessionRepository {
    private final DatabaseConnector connector;
    private final Executor ioExecutor;

    /**
     * Instantiates SqlSessionRepository.
     */
    public SqlSessionRepository(DatabaseConnector connector, Executor ioExecutor) {
        this.connector = connector;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public CompletableFuture<Optional<Session>> findById(SessionId id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "SELECT user_uuid, ip_address, created_at, expires_at "
                        + "FROM gatotauth_sessions WHERE session_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, id.token());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        UUID userUuid = UUID.fromString(rs.getString("user_uuid"));
                        String ipAddress = rs.getString("ip_address");
                        Instant createdAt = Instant.parse(rs.getString("created_at"));
                        Instant expiresAt = Instant.parse(rs.getString("expires_at"));

                        return Optional.of(new Session(id, userUuid, ipAddress, createdAt, expiresAt));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error loading session by ID", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Void> save(Session session) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "INSERT INTO gatotauth_sessions "
                        + "(session_id, user_uuid, ip_address, created_at, expires_at) "
                        + "VALUES (?, ?, ?, ?, ?) ON CONFLICT(session_id) DO UPDATE SET expires_at = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, session.sessionId().token());
                    ps.setString(2, session.userUuid().toString());
                    ps.setString(3, session.ipAddress());
                    ps.setString(4, session.createdAt().toString());
                    ps.setString(5, session.expiresAt().toString());
                    ps.setString(6, session.expiresAt().toString());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error saving session to SQL database", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Void> delete(SessionId id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = connector.getConnection()) {
                String query = "DELETE FROM gatotauth_sessions WHERE session_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, id.token());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting session by ID", e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<List<Session>> findByAccountId(AccountId accountId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Session> list = new ArrayList<>();
            try (Connection conn = connector.getConnection()) {
                String query = "SELECT session_id, ip_address, created_at, expires_at "
                        + "FROM gatotauth_sessions WHERE user_uuid = ?;";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, accountId.value().toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            SessionId sessionId = new SessionId(rs.getString("session_id"));
                            String ipAddress = rs.getString("ip_address");
                            Instant createdAt = Instant.parse(rs.getString("created_at"));
                            Instant expiresAt = Instant.parse(rs.getString("expires_at"));

                            list.add(new Session(sessionId, accountId.value(), ipAddress, createdAt, expiresAt));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error loading sessions by AccountId", e);
            }
            return list;
        }, ioExecutor);
    }
}
