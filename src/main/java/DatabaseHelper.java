package src.main.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles creation and lifecycle management of the PostgreSQL database.
 * <p>
 * This class opens a JDBC connection to PostgreSQL, creates the required tables
 * if they do not yet exist, and exposes the live {@link Connection}.
 */
public class DatabaseHelper {
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/parla";
    private static final String DEFAULT_USER = "parla";
    private static final String DEFAULT_PASSWORD = "parla";

    private Connection conn;

    /**
     * Opens the PostgreSQL database connection and ensures the schema exists.
     * <p>
     * It reads configuration from environment variables:
     * <ul>
     *   <li>{@code PARLA_DB_URL}</li>
     *   <li>{@code PARLA_DB_USER}</li>
     *   <li>{@code PARLA_DB_PASSWORD}</li>
     * </ul>
     *
     * @throws SQLException if the database cannot be opened or initialized
     */
    public void connect() throws SQLException {
        String url = System.getenv().getOrDefault("PARLA_DB_URL", DEFAULT_URL);
        String user = System.getenv().getOrDefault("PARLA_DB_USER", DEFAULT_USER);
        String password = System.getenv().getOrDefault("PARLA_DB_PASSWORD", DEFAULT_PASSWORD);

        conn = DriverManager.getConnection(url, user, password);
        initializeSchema();
    }

    /**
     * Creates the required tables if they do not already exist.
     *
     * @throws SQLException if table creation fails
     */
    private void initializeSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS words ("
                    + "id SERIAL PRIMARY KEY, "
                    + "language TEXT NOT NULL, "
                    + "foreign_word TEXT NOT NULL, "
                    + "english TEXT NOT NULL, "
                    + "lookups INTEGER NOT NULL, "
                    + "added_date TEXT NOT NULL"
                    + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quizzes ("
                    + "id SERIAL PRIMARY KEY, "
                    + "date TEXT NOT NULL, "
                    + "score INTEGER NOT NULL, "
                    + "num_questions INTEGER NOT NULL"
                    + ")");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_words ("
                    + "id SERIAL PRIMARY KEY, "
                    + "quiz_id INTEGER NOT NULL REFERENCES quizzes(id), "
                    + "foreign_word TEXT NOT NULL, "
                    + "translation TEXT NOT NULL"
                    + ")");
        }
    }

    /**
     * Returns the active database connection.
     *
     * @return the open {@link Connection}
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Closes the database connection if it is open.
     *
     * @throws SQLException if closing the connection fails
     */
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
