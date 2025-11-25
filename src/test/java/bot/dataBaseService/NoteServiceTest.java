package bot.dataBaseService;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class NoteServiceTest {
    
    private static final String TEST_DB_URL = "jdbc:sqlite:test_database/notes_test.db";
    private NoteService noteService;
    
    @BeforeAll
    //перед тестами создаем тестовую бд
    static void setUpBeforeAll() throws Exception {
        // Создаём тестовую директорию
        java.nio.file.Path testDbDir = java.nio.file.Paths.get("test_database");
        if (!java.nio.file.Files.exists(testDbDir)) {
            java.nio.file.Files.createDirectories(testDbDir);
        }
    }
    
    @BeforeEach
    //перед каждым тестом новый NoteService + очистка бд 
    void setUp() throws SQLException {
        // Создаём сервис с тестовой БД
        noteService = new NoteService(TEST_DB_URL);
        clearTestData();
    }
    
    @AfterEach
    //после теста очистка бд
    void tearDown() throws SQLException {
        clearTestData();
    }
    
    private void clearTestData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM notes");
        }
    }
    
    private void insertTestNote(Long userId, String noteName, String text) throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL);
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO notes (user_id, note_name, text) VALUES (?, ?, ?)")) {
            
            stmt.setLong(1, userId);
            stmt.setString(2, noteName);
            stmt.setString(3, text);
            stmt.executeUpdate();
        }
    }
    
    @Test
    //добавить
    void testAddNote() throws SQLException {
        // Given
        Long userId = 123L;
        String noteName = "test_note";
        String text = "Это тестовая заметка";
        
        // When
        noteService.addNoteToDB(userId, noteName, text);
        
        // Then
        String retrievedText = noteService.getNote(userId, noteName);
        assertThat(retrievedText).isEqualTo(text);
        assertThat(noteService.noteExists(userId, noteName)).isTrue();
    }
    
    @Test
    //получение существующей
    void testGetNote_WhenNoteExists() throws SQLException {
        // Given
        Long userId = 456L;
        String noteName = "existing_note";
        String expectedText = "Существующая заметка";
        insertTestNote(userId, noteName, expectedText);
        
        // When
        String actualText = noteService.getNote(userId, noteName);
        
        // Then
        assertThat(actualText).isEqualTo(expectedText);
    }
    
    @Test
    //получение НЕсуществующей
    void testGetNote_WhenNoteNotExists() throws SQLException {
        // Given
        Long userId = 999L;
        String noteName = "non_existing_note";
        
        // When
        String result = noteService.getNote(userId, noteName);
        
        // Then
        assertThat(result).isNull();
        assertThat(noteService.noteExists(userId, noteName)).isFalse();
    }
    
    @Test
    //удаление 
    void testRemoveNote() throws SQLException {
        // Given
        Long userId = 789L;
        String noteName = "note_to_remove";
        String text = "Заметка для удаления";
        insertTestNote(userId, noteName, text);
        
        // Предварительная проверка
        assertThat(noteService.getNote(userId, noteName)).isEqualTo(text);
        assertThat(noteService.noteExists(userId, noteName)).isTrue();
        
        // When
        noteService.removeNoteFromDB(userId, noteName);
        
        // Then
        String result = noteService.getNote(userId, noteName);
        assertThat(result).isNull();
        assertThat(noteService.noteExists(userId, noteName)).isFalse();
    }
    
    @Test
    //записки пользователя
    void testGetUserNotes() throws SQLException {
        // Given
        Long userId = 111L;
        insertTestNote(userId, "note1", "Текст 1");
        insertTestNote(userId, "note2", "Текст 2");
        insertTestNote(userId, "note3", "Текст 3");
        
        // Добавляем заметку другому пользователю
        insertTestNote(222L, "other_user_note", "Чужая заметка");
        
        // When
        List<String> userNotes = noteService.getUserNotes(userId);
        
        // Then
        assertThat(userNotes)
            .hasSize(3)
            .containsExactly("note1", "note2", "note3");
    }
    
    @Test
    //записки "чистого" пользователя
    void testGetUserNotes_WhenNoNotes() throws SQLException {
        // Given
        Long userId = 333L;
        
        // When
        List<String> userNotes = noteService.getUserNotes(userId);
        
        // Then
        assertThat(userNotes).isEmpty();
    }
    
    @Test
    //обновление записки 
    void testAddNote_UpdateExistingNote() throws SQLException {
        // Given
        Long userId = 444L;
        String noteName = "duplicate_note";
        String firstText = "Первая запись";
        String secondText = "Обновлённая запись";
        
        // When - добавляем первую заметку
        noteService.addNoteToDB(userId, noteName, firstText);
        
        // Then - обновляем существующую заметку
        noteService.addNoteToDB(userId, noteName, secondText);
        
        String result = noteService.getNote(userId, noteName);
        assertThat(result).isEqualTo(secondText);
        
        // Проверяем, что запись действительно обновилась, а не добавилась вторая
        List<String> userNotes = noteService.getUserNotes(userId);
        assertThat(userNotes).hasSize(1).containsExactly(noteName);
    }

}