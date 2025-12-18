package bot.dataBaseService;

import bot.models.Reminder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class NoteServiceTest {

    private NoteService noteService;
    private static final String TEST_DB_URL = "jdbc:sqlite:target/test-note.db";

    @BeforeEach
    void setUp() throws IOException {
        Path dbPath = Path.of("target/test-note.db");
        Files.deleteIfExists(dbPath);
        Files.createDirectories(dbPath.getParent());
        noteService = new NoteService(TEST_DB_URL);
    }

    // ------- ТЕСТЫ ДЛЯ ОСНОВНЫХ ОПЕРАЦИЙ --------

    @Test
    // Проверка добавления заметки
    // Ожидаем: заметка сохранена и может быть получена
    void testAddNote() throws SQLException {
        Long userId = 123L;
        String noteName = "test_note";
        String text = "Это тестовая заметка";

        noteService.addNoteToDB(userId, noteName, text, null);
        String retrievedText = noteService.getNote(userId, noteName);

        assertThat(retrievedText).isEqualTo(text);
    }

    @Test
    // Получение существующей заметки
    // Ожидаем: корректно возвращается заметка
    void testGetNote_WhenNoteExists() throws SQLException {
        Long userId = 456L;
        String noteName = "existing_note";
        String expectedText = "Существующая заметка";

        noteService.addNoteToDB(userId, noteName, expectedText, null);
        String actualText = noteService.getNote(userId, noteName);

        assertThat(actualText).isEqualTo(expectedText);
    }

    @Test
    // Получение несуществующей заметки
    // Ожидаем: вернется null
    void testGetNote_WhenNoteNotExists() throws SQLException {
        String result = noteService.getNote(999L, "non_existing_note");
        assertThat(result).isNull();
    }

    @Test
    // Удаление заметки
    // Ожидаем: заметка успешно удаляется
    void testRemoveNote() throws SQLException {
        Long userId = 789L;
        String noteName = "note_to_remove";
        String text = "Заметка для удаления";

        noteService.addNoteToDB(userId, noteName, text, null);
        assertThat(noteService.getNote(userId, noteName)).isEqualTo(text);

        noteService.removeNoteFromDB(userId, noteName);
        assertThat(noteService.getNote(userId, noteName)).isNull();
    }

    @Test
    // Получение списка всех заметок юзера
    // Ожидаем: вернутся заметки только нужного пользователя
    void testGetUserNotes() throws SQLException {
        Long userId = 111L;
        noteService.addNoteToDB(userId, "note1", "Текст 1", null);
        noteService.addNoteToDB(userId, "note2", "Текст 2", null);
        noteService.addNoteToDB(userId, "note3", "Текст 3", null);
        noteService.addNoteToDB(222L, "other_user_note", "Чужая заметка", null);

        List<String> userNotes = noteService.getUserNotes(userId);

        assertThat(userNotes)
                .hasSize(3)
                .containsExactlyInAnyOrder("note1", "note2", "note3");
    }

    @Test
    // Получение заметок несуществующего пользователя
    // Ожидаем: вернется пустой список
    void testGetUserNotes_WhenNoNotes() throws SQLException {
        List<String> userNotes = noteService.getUserNotes(333L);
        assertThat(userNotes).isEmpty();
    }

    // --------ТЕСТЫ ДЛЯ НАПОМИНАНИЙ--------

    @Test
    // Проверка просроченных напоминаний
    // Ожидаем: возвращаются просроченные заметки
    void testGetDueReminders_ReturnsPastReminders() throws SQLException {
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        noteService.addNoteToDB(123L, "Прошлое", "Текст1", pastTime);

        List<Reminder> reminders = noteService.getDueReminders();

        assertThat(reminders)
                .isNotEmpty()
                .first()
                .extracting(Reminder::getContent)
                .isEqualTo("[Прошлое] Текст1");
    }

    @Test
    // Проверка, что напоминания из будущего не вызываются
    // Ожидаем: пустой список
    void testGetDueReminders_ReturnsEmptyForFutureReminders() throws SQLException {
        noteService.addNoteToDB(123L, "Будущее", "Текст",
                LocalDateTime.now().plusHours(1));

        List<Reminder> due = noteService.getDueReminders();

        assertThat(due).isEmpty();
    }

    @Test
    // Проверка игнорирования заметок без напоминания
    // Ожидаем: пустой список
    void testGetDueReminders_ReturnsEmptyForNotesWithoutReminder() throws SQLException {
        noteService.addNoteToDB(123L, "Обычная", "Текст", null);

        List<Reminder> due = noteService.getDueReminders();

        assertThat(due).isEmpty();
    }

    @Test
    // Проверка заметок для разных пользователей
    // Ожидаем: все просроченные напоминания возвращаются
    void testGetDueReminders_ReturnsMultiplePastReminders() throws SQLException {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        noteService.addNoteToDB(100L, "Заметка1", "Текст1", pastTime);
        noteService.addNoteToDB(100L, "Заметка2", "Текст2", pastTime.minusMinutes(30));
        noteService.addNoteToDB(200L, "Заметка3", "Текст3", pastTime);

        List<Reminder> reminders = noteService.getDueReminders();

        assertThat(reminders).hasSize(3);

        // Проверяем формат контента
        assertThat(reminders)
                .extracting(Reminder::getContent)
                .containsExactlyInAnyOrder(
                        "[Заметка1] Текст1",
                        "[Заметка2] Текст2",
                        "[Заметка3] Текст3"
                );
    }

    @Test
    // Проверка корректного заполнения полей Reminder
    // Ожидаем: все поля заполнены правильно
    void testGetDueReminders_ReminderPropertiesAreCorrect() throws SQLException {
        LocalDateTime localTime = LocalDateTime.now().minusMinutes(30);

        // Конвертируем локальное время в UTC для сравнения
        LocalDateTime utcTime = localTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime()
                .withNano(0); // БД округляет наносекунды

        noteService.addNoteToDB(555L, "Тест", "Проверка свойств", localTime);

        List<Reminder> reminders = noteService.getDueReminders();

        assertThat(reminders).hasSize(1);

        Reminder reminder = reminders.get(0);
        assertThat(reminder.getGroupId()).isEqualTo(555);
        assertThat(reminder.getType()).isEqualTo(Reminder.Type.SINGLE);
        assertThat(reminder.isEnabled()).isTrue();
        assertThat(reminder.getTriggerTime()).isEqualTo(utcTime);
    }

    // ------ ТЕСТ ХРАНЕНИЯ ВРЕМЕНИ -------

    @Test
    // Ожидаем: рассчитываем, каким должно быть время в UTC
    /* Пример:
        Если часовой пояс: MSK (UTC+3)
        Локальное время: 2025-12-10T10:00 (MSK)
        Ожидаемое UTC время: 2025-12-10T07:00 (минус 3 часа)
     */
    @Disabled("Enable only if NoteService converts time to UTC on save")
    void testAddNoteStoresReminderTimeInUTC() throws SQLException {
        // Этот тест оставляем только если в NoteService есть конвертация в UTC
        // Если нет - удаляем или комментируем

        LocalDateTime localTime = LocalDateTime.of(2025, 12, 10, 10, 0);
        noteService.addNoteToDB(123L, "Тест", "Текст", localTime);

        String sql = "SELECT remind_at FROM notes WHERE user_id = 123 AND note_name = 'Тест'";
        try (var conn = noteService.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            assertThat(rs.next()).isTrue();
            String dbTimeStr = rs.getString("remind_at");
            LocalDateTime dbTime = LocalDateTime.parse(dbTimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            LocalDateTime expectedUTC = localTime.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();

            assertThat(dbTime).isEqualTo(expectedUTC);
        }
    }

    // ------- ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ ГРАНИЧНЫХ СЛУЧАЕВ --------

    @Test
    // Проверка перезаписи существующей заметки
    // Ожидаем: старая заметка заменяется новой
    void testAddNoteOverwritesExistingNote() throws SQLException {
        Long userId = 777L;
        String noteName = "дубликат";

        noteService.addNoteToDB(userId, noteName, "Первый текст", null);
        noteService.addNoteToDB(userId, noteName, "Второй текст", null);

        String result = noteService.getNote(userId, noteName);
        assertThat(result).isEqualTo("Второй текст");
    }

    @Test
    // Проверка, что удаленная заметка не создает напоминания
    // Ожидаем: после удаления заметки напоминание не возвращается
    void testGetDueReminders_AfterNoteRemoval() throws SQLException {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
        Long userId = 888L;

        noteService.addNoteToDB(userId, "Оставить", "Текст1", pastTime);
        noteService.addNoteToDB(userId, "Удалить", "Текст2", pastTime);

        // Удаляем одну заметку
        noteService.removeNoteFromDB(userId, "Удалить");

        List<Reminder> reminders = noteService.getDueReminders();

        assertThat(reminders)
                .hasSize(1)
                .extracting(Reminder::getContent)
                .containsExactly("[Оставить] Текст1");
    }
}