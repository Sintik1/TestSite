import java.sql.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилитный класс для работы с базой данных в тестах.
 * Используется для проверки создания и удаления записей в БД.
 */
public class DatabaseHelper {

    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/goals_db";
    private static final String DEFAULT_DB_USER = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final String DEFAULT_TABLE_NAME = "categories";
    private static final String DEFAULT_NAME_COLUMN = "name";
    private static final String DEFAULT_ID_COLUMN = "id";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String tableName;
    private final String nameColumn;
    private final String idColumn;

    /**
     * Чтобы не пытаться создавать БД на каждый запрос.
     * В тестах обычно один процесс JVM, поэтому volatile достаточно.
     */
    private volatile boolean databaseEnsured = false;

    // jdbc:postgresql://host:port/dbName?params
    private static final Pattern PG_JDBC_URL =
            Pattern.compile("^jdbc:postgresql://([^/:?#]+)(?::(\\d+))?/([^?]+)(\\?.*)?$");

    /**
     * Создает экземпляр DatabaseHelper с настройками из переменных окружения или значениями по умолчанию.
     */
    public DatabaseHelper() {
        this.dbUrl = System.getenv().getOrDefault("DB_URL",
                System.getProperty("db.url", DEFAULT_DB_URL));
        this.dbUser = System.getenv().getOrDefault("DB_USER",
                System.getProperty("db.user", DEFAULT_DB_USER));
        this.dbPassword = System.getenv().getOrDefault("DB_PASSWORD",
                System.getProperty("db.password", DEFAULT_DB_PASSWORD));
        this.tableName = System.getenv().getOrDefault("DB_TABLE",
                System.getProperty("db.table", DEFAULT_TABLE_NAME));
        this.nameColumn = System.getenv().getOrDefault("DB_NAME_COLUMN",
                System.getProperty("db.name.column", DEFAULT_NAME_COLUMN));
        this.idColumn = System.getenv().getOrDefault("DB_ID_COLUMN",
                System.getProperty("db.id.column", DEFAULT_ID_COLUMN));
    }

    /**
     * Создает подключение к базе данных.
     */
    private Connection getConnection() throws SQLException {
        // В CI часто поднимают Postgres без нужной БД. Попробуем создать goals_db автоматически.
        // Если прав нет — просто попробуем подключиться как есть и отдадим исходную ошибку.
        if (!databaseEnsured) {
            synchronized (this) {
                if (!databaseEnsured) {
                    ensureDatabaseExistsIfMissing();
                    databaseEnsured = true;
                }
            }
        }

        return openConnection(dbUrl);
    }

    private Connection openConnection(String url) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        return DriverManager.getConnection(url, props);
    }

    /**
     * Если указанная в dbUrl БД отсутствует (SQLSTATE 3D000), создаём её через подключение к postgres.
     * Работает только для PostgreSQL JDBC URL формата jdbc:postgresql://host:port/dbName[?params].
     */
    private void ensureDatabaseExistsIfMissing() {
        PgJdbcInfo info = parsePostgresJdbcUrl(dbUrl);
        if (info == null) {
            return; // не PostgreSQL или нестандартный URL — не лезем
        }

        // Быстрая попытка подключения к целевой БД
        try (Connection ignored = openConnection(dbUrl)) {
            return;
        } catch (SQLException e) {
            // 3D000 = invalid_catalog_name (database does not exist)
            if (!"3D000".equals(e.getSQLState())) {
                return;
            }
        }

        String adminUrl = buildPostgresJdbcUrl(info.host, info.port, "postgres", info.query);
        String quotedDbName = quoteIdentifier(info.database);
        String createSql = "CREATE DATABASE " + quotedDbName;

        try (Connection adminConn = openConnection(adminUrl);
             Statement st = adminConn.createStatement()) {
            st.executeUpdate(createSql);
        } catch (SQLException createEx) {
            // В параллельном запуске БД могла уже появиться, или нет прав.
            // 42P04 = duplicate_database
            if (!"42P04".equals(createEx.getSQLState())) {
                System.err.println("Не удалось создать БД '" + info.database + "': " + createEx.getMessage());
            }
        }
    }

    private static String quoteIdentifier(String identifier) {
        // Минимальная защита от кавычек в имени БД
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private static String buildPostgresJdbcUrl(String host, int port, String database, String query) {
        String q = (query == null) ? "" : query;
        return "jdbc:postgresql://" + host + ":" + port + "/" + database + q;
    }

    private static PgJdbcInfo parsePostgresJdbcUrl(String url) {
        if (url == null) return null;
        Matcher m = PG_JDBC_URL.matcher(url.trim());
        if (!m.matches()) return null;

        String host = m.group(1);
        String portStr = m.group(2);
        int port = (portStr == null || portStr.isBlank()) ? 5432 : Integer.parseInt(portStr);
        String db = m.group(3);
        String query = m.group(4); // includes leading '?', if present
        return new PgJdbcInfo(host, port, db, query);
    }

    private static final class PgJdbcInfo {
        final String host;
        final int port;
        final String database;
        final String query;

        private PgJdbcInfo(String host, int port, String database, String query) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.query = query;
        }
    }

    /**
     * Проверяет, существует ли запись с указанным именем в таблице.
     *
     * @param name имя блока цели для поиска
     * @return true если запись найдена, false если нет
     */
    public boolean goalExists(String name) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + nameColumn + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования записи в БД: " + e.getMessage());
            e.printStackTrace();
            // В случае ошибки подключения к БД возвращаем false, чтобы не падать тесты
            return false;
        }

        return false;
    }

    /**
     * Получает ID записи по имени.
     *
     * @param name имя блока цели
     * @return ID записи или null если не найдена
     */
    public Long getGoalId(String name) {
        String sql = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + nameColumn + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(idColumn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID записи из БД: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Удаляет запись по имени из базы данных.
     *
     * @param name имя блока цели для удаления
     * @return true если запись удалена, false если не найдена или произошла ошибка
     */
    public boolean deleteGoal(String name) {
        String sql = "DELETE FROM " + tableName + " WHERE " + nameColumn + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении записи из БД: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Удаляет запись по ID из базы данных.
     *
     * @param id ID записи для удаления
     * @return true если запись удалена, false если не найдена или произошла ошибка
     */
    public boolean deleteGoalById(Long id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении записи из БД по ID: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Проверяет подключение к базе данных.
     *
     * @return true если подключение успешно, false если нет
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5); // проверка в течение 5 секунд
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
            return false;
        }
    }

    /**
     * Получает количество записей в таблице.
     *
     * @return количество записей
     */
    public int getGoalCount() {
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете записей в БД: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
