package bot.dataBaseService;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import bot.models.NoteReminder;

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
        	//загрузка драйвера БД
            Class.forName("org.sqlite.JDBC");
            System.out.println(" SQLite JDBC загружен");

            // Определяем путь к папке из URL базы данных
            String dbPath = dbUrl.replace("jdbc:sqlite:", "");
            java.nio.file.Path dbDir = java.nio.file.Paths.get(dbPath).getParent();
            
            if (dbDir != null && !java.nio.file.Files.exists(dbDir)) {
                System.out.println(" Папка '" + dbDir + "' не найдена — создаём...");
                java.nio.file.Files.createDirectories(dbDir);
            }
            
            //создаем таблицу 
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

            	stmt.execute("""
            		    CREATE TABLE IF NOT EXISTS notes (
            		        user_id INTEGER NOT NULL,
            		        note_name TEXT NOT NULL,
            		        text TEXT NOT NULL,
            		        remind_at DATETIME,
            		        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            		        PRIMARY KEY (user_id, note_name)
            		    )
            		""");

                System.out.println(" Таблица 'notes' создана или уже существует");
            }

        } catch (Exception e) {
            System.err.println(" КРИТИЧЕСКАЯ ОШИБКА: не удалось инициализировать NoteService!");
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
    public void addNoteToDB(Long userId, String noteName, String text, LocalDateTime remindAt) throws SQLException {
        String sql = "INSERT OR REPLACE INTO notes (user_id, note_name, text, remind_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.setString(3, text);

            if (remindAt != null) {
                // 🔧 Преобразуем в UTC и форматируем
                ZonedDateTime zoned = remindAt.atZone(ZoneId.systemDefault());
                ZonedDateTime utcTime = zoned.withZoneSameInstant(ZoneOffset.UTC);
                String formatted = utcTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                stmt.setString(4, formatted);
            } else {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            }

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
    
    public List<NoteReminder> getDueReminders() throws SQLException {
        String sql = "SELECT user_id, note_name, text FROM notes WHERE remind_at IS NOT NULL AND remind_at <= CURRENT_TIMESTAMP";
        List<NoteReminder> reminders = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reminders.add(new NoteReminder(
                    rs.getLong("user_id"),
                    rs.getString("note_name"),
                    rs.getString("text")
                ));
            }
        }
        return reminders;
    }
}