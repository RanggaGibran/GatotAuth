package dev.gatotauth.core.infra.storage.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * DatabaseConnector wraps the HikariCP DataSource connection pool and runs migrations.
 */
public final class DatabaseConnector implements AutoCloseable {
    private final HikariDataSource dataSource;

    /**
     * Initializes the SQLite datasource at the specified path and runs migrations.
     */
    public DatabaseConnector(String dbFilePath) {
        Objects.requireNonNull(dbFilePath, "Database file path cannot be null");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFilePath);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("SELECT 1");

        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");

        this.dataSource = new HikariDataSource(config);
        runMigrations();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void runMigrations() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("CREATE TABLE IF NOT EXISTS gatotauth_migrations ("
                    + "version INT PRIMARY KEY,"
                    + "migration_name VARCHAR(128) NOT NULL,"
                    + "applied_at VARCHAR(64) NOT NULL"
                    + ");");

            Set<Integer> appliedVersions = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("SELECT version FROM gatotauth_migrations;")) {
                while (rs.next()) {
                    appliedVersions.add(rs.getInt("version"));
                }
            }

            Migration[] migrations = new Migration[] {
                new Migration(1, "create_accounts_table",
                    "CREATE TABLE IF NOT EXISTS gatotauth_accounts ("
                    + "id VARCHAR(36) PRIMARY KEY,"
                    + "created_at TIMESTAMP NOT NULL,"
                    + "locked BOOLEAN NOT NULL"
                    + ");"),
                new Migration(2, "create_identities_table",
                    "CREATE TABLE IF NOT EXISTS gatotauth_identities ("
                    + "account_id VARCHAR(36) NOT NULL,"
                    + "provider_type VARCHAR(64) NOT NULL,"
                    + "identifier VARCHAR(128) NOT NULL,"
                    + "username VARCHAR(128) NOT NULL,"
                    + "PRIMARY KEY (account_id, provider_type, identifier),"
                    + "FOREIGN KEY (account_id) REFERENCES gatotauth_accounts(id) ON DELETE CASCADE"
                    + ");"),
                new Migration(3, "create_credentials_table",
                    "CREATE TABLE IF NOT EXISTS gatotauth_credentials ("
                    + "account_id VARCHAR(36) NOT NULL,"
                    + "credential_type VARCHAR(64) NOT NULL,"
                    + "credential_value TEXT NOT NULL,"
                    + "PRIMARY KEY (account_id, credential_type),"
                    + "FOREIGN KEY (account_id) REFERENCES gatotauth_accounts(id) ON DELETE CASCADE"
                    + ");"),
                new Migration(4, "create_sessions_table",
                    "CREATE TABLE IF NOT EXISTS gatotauth_sessions ("
                    + "session_id VARCHAR(64) PRIMARY KEY,"
                    + "user_uuid VARCHAR(36) NOT NULL,"
                    + "ip_address VARCHAR(45) NOT NULL,"
                    + "created_at TIMESTAMP NOT NULL,"
                    + "expires_at TIMESTAMP NOT NULL"
                    + ");")
            };

            for (Migration m : migrations) {
                if (!appliedVersions.contains(m.version)) {
                    conn.setAutoCommit(false);
                    try {
                        stmt.execute(m.sql);
                        String insertSql = "INSERT INTO gatotauth_migrations "
                                + "(version, migration_name, applied_at) VALUES (?, ?, ?);";
                        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                            ps.setInt(1, m.version);
                            ps.setString(2, m.name);
                            ps.setString(3, Instant.now().toString());
                            ps.executeUpdate();
                        }
                        conn.commit();
                    } catch (Exception e) {
                        conn.rollback();
                        throw e;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to run database migrations", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private record Migration(int version, String name, String sql) {}
}
