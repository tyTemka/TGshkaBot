package bot.commands;

import bot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import bot.dataBaseService.NoteService;
import bot.utilits.SharedNoteRegistry;
import bot.models.SharedNoteData;

import static org.junit.jupiter.api.Assertions.*;

class ShareCommandTest {

    // Тестовый бот
    private static class TestTelegramBot extends TelegramBot { //наследуемся от класса TelegramBot
        public String lastMessage; //сохраняем для проверки
        public Long lastChatId; //сохраняем для проверки
        public boolean noteExistsResult = true;
        public String registeredId;

        private final NoteService testNoteService = new NoteService() { //возвращаем упрощенный NoteService
            @Override
            public boolean noteExists(Long userId, String noteName) {
                return noteExistsResult;
            }
        };

        private final SharedNoteRegistry testSharedNoteRegistry = new SharedNoteRegistry() { //генерируем токен
            @Override
            public String register(long ownerId, String noteName) {
                registeredId = "share_test_" + ownerId + "_" + noteName;
                return registeredId;
            }

            @Override
            public SharedNoteData consume(String id) {
                return null;
            }
        };

        @Override
        public void sendMessage(Long chatId, String text) {
            this.lastChatId = chatId;
            this.lastMessage = text;
        }

        @Override
        public NoteService getNoteService() {
            return testNoteService;
        }

        @Override
        public SharedNoteRegistry getSharedNoteRegistry() {
            return testSharedNoteRegistry;
        }
    }

    // Тестовые сообщения без Mockito
    private static class TestMessage extends Message {
        private Long chatId;
        private User from;

        public TestMessage(Long chatId, Long userId) {
            this.chatId = chatId;
            this.from = new TestUser(userId);
        }

        @Override
        public Long getChatId() {
            return chatId;
        }

        @Override
        public User getFrom() {
            return from;
        }
    }

    private static class TestUser extends User {
        private Long id;

        public TestUser(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    @Test
    //Тестинг методы интерфейса
    // Ожидаем: вернется корректные getCommandName, getDescription, getUsage
    void properties_areCorrect() {
        ShareCommand command = new ShareCommand();
        assertEquals("share", command.getCommandName());
        assertEquals("Поделиться заметкой по ссылке", command.getDescription());
        assertEquals("/share <имя_заметки>", command.getUsage());
    }

    @Test
    // Тестинг формирования ссылки
    // Ожидаем: заметка существует и бот сформирует на нее ссылку
    void execute_validNote_createsLink() {
        ShareCommand command = new ShareCommand();
        TestTelegramBot bot = new TestTelegramBot();
        bot.noteExistsResult = true;

        Message message = new TestMessage(123L, 456L);

        command.execute(bot, message, new String[]{"Заметка"});

        assertNotNull(bot.lastMessage);
        assertTrue(bot.lastMessage.contains("https://t.me/OOPOOP_tg_bot?start=share_share_test_456_Заметка"));
        assertEquals(123L, bot.lastChatId);
    }

    @Test
    // Тестинг отсутствующей заметки
    // Ожидаем: если заметки нет, вывыдется сообщение и ссылка не сформируется
    void execute_noteNotFound_showsError() {
        // Arrange
        ShareCommand command = new ShareCommand();
        TestTelegramBot bot = new TestTelegramBot();
        bot.noteExistsResult = false;

        Message message = new TestMessage(123L, 456L);

        // Act
        command.execute(bot, message, new String[]{"Несуществующая"});

        // Assert
        assertNotNull(bot.lastMessage);
        assertTrue(bot.lastMessage.contains("❌ Заметка «Несуществующая» не найдена"));
    }

    @Test
    // Тестинг нуль-аргументов
    // Ожидаем: если пользователь ввел команду шейр без аргумента выведется подсказка
    void execute_withoutArgs_showsUsage() {
        ShareCommand command = new ShareCommand();
        TestTelegramBot bot = new TestTelegramBot();

        Message message = new TestMessage(123L, 456L);

        command.execute(bot, message, new String[]{});
        
        assertNotNull(bot.lastMessage);
        assertTrue(bot.lastMessage.contains("📌 Укажите имя заметки"));
        assertTrue(bot.lastMessage.contains("/share Список_дел"));
    }
}