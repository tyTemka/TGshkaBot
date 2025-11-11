package bot.dataBaseService;

import java.sql.*;
import java.util.*;

public class NoteService {
    
    private final String dbUrl;
    
    // Основной конструктор для продакшн
    public NoteService() {
        this("jdbc:sqlite:database/notes.db");
    }
    
    // Конструктор для тестов
    public NoteService(String testDbUrl) {
        this.dbUrl = testDbUrl;
        initializeDatabase();
    }
    //вспомогательный для создания бд
    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("✅ SQLite JDBC загружен");

            // Определяем путь к папке из URL базы данных
            String dbPath = dbUrl.replace("jdbc:sqlite:", "");
            java.nio.file.Path dbDir = java.nio.file.Paths.get(dbPath).getParent();
            
            if (dbDir != null && !java.nio.file.Files.exists(dbDir)) {
                System.out.println("📂 Папка '" + dbDir + "' не найдена — создаём...");
                java.nio.file.Files.createDirectories(dbDir);
            }

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS notes (
                        user_id INTEGER NOT NULL,
                        note_name TEXT NOT NULL,
                        text TEXT NOT NULL,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (user_id, note_name)
                    )
                    """);
                System.out.println("✅ Таблица 'notes' создана или уже существует");
            }

        } catch (Exception e) {
            System.err.println("❌ КРИТИЧЕСКАЯ ОШИБКА: не удалось инициализировать NoteService!");
            e.printStackTrace();
            throw new RuntimeException("Инициализация NoteService провалена", e);
        }    
    }
    //вспомогательный для подключения 
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
    //основной удаления
    public void removeNoteFromDB(Long userId, String noteName) throws SQLException {
        String sql = "DELETE FROM notes WHERE user_id = ? AND note_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.executeUpdate();
        }
    }
    //основной получения заметки
    public String getNote(Long userId, String noteName) throws SQLException {
        String sql = "SELECT text FROM notes WHERE user_id = ? AND note_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("text");
                }
                return null;
            }
        }
    }
    //получение заметок юзера
    public List<String> getUserNotes(Long userId) throws SQLException {
        String sql = "SELECT note_name FROM notes WHERE user_id = ? ORDER BY note_name";
        List<String> noteNames = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    noteNames.add(rs.getString("note_name"));
                }
            }
        }
        return noteNames;
    }
    //добавить заметку
    public void addNoteToDB(Long userId, String noteName, String text) throws SQLException {
        // Используем INSERT OR REPLACE для обновления существующих записей
        String sql = "INSERT OR REPLACE INTO notes (user_id, note_name, text) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.setString(3, text);
            stmt.executeUpdate();
        }
    }
    
    //метод для проверки существования заметки
    public boolean noteExists(Long userId, String noteName) throws SQLException {
        String sql = "SELECT 1 FROM notes WHERE user_id = ? AND note_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}