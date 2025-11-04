package bot.dataBaseService;

import java.sql.*;
import java.util.*;

public class NoteService {
	
	private static final String DB_URL = "jdbc:sqlite:database/notes.db";
	
	// Для драйверов JDBC ниже 4
	static {
	    try {
	        Class.forName("org.sqlite.JDBC");
	    } catch (ClassNotFoundException e) {
	        throw new RuntimeException("SQLite JDBC driver not found", e);
	    }
	}
	
	// Добавление заметки по userId и noteName, primarykey(userId, noteName)
    public void addNoteToDB(Long userId, String noteName, String text) throws SQLException {
        String sql = "INSERT INTO notes (user_id, note_name, text) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.setString(3, text);
            stmt.executeUpdate();
        }
    }
    
    // Удаление заметки по userId и noteName
    public void removeNoteFromDB(Long userId, String noteName) throws SQLException {
        String sql = "DELETE FROM notes WHERE user_id = ? AND note_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.executeUpdate();
        }
    }
    
    // Возвращает текст заметки по userId и noteName
    public String getNote(Long userId, String noteName) throws SQLException {
        String sql = "SELECT text FROM notes WHERE user_id = ? AND note_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
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
    
    // Получение списка имён заметок пользователя
    public List<String> getUserNotes(Long userId) throws SQLException {
        String sql = "SELECT note_name FROM notes WHERE user_id = ? ORDER BY note_name";
        List<String> noteNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
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
}
