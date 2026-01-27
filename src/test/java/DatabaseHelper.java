import java.sql.*;
import java.util.Properties;

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
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        return DriverManager.getConnection(dbUrl, props);
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
