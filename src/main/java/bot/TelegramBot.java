package bot;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import bot.commands.Command;
import bot.commands.InputHandler;
import bot.commands.CommandRegistry;
import bot.dataBaseService.NoteService;
import bot.models.SharedNoteData;
import bot.utilits.SharedNoteRegistry;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TelegramBot extends TelegramLongPollingBot { //оправшиваем сервер на предмет вхождения сообщений 

    private final String botToken = System.getenv("TELEGRAM_BOT_TOKEN");
    private final NoteService noteService = new NoteService();
    public TelegramBot() {
    	
    }

    @Override
    public String getBotUsername() {
        return "OOPOOP_tg_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
    
    // Обработчик ожиданий через HashMap
    private final Map<Long, InputHandler> pendingInputHandlers = new HashMap<>();
    
    public void setPendingInputHandler(Long chatId, InputHandler handler) {
        pendingInputHandlers.put(chatId, handler);
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        
        if (!message.hasText()) {
            return;
        }

        String text = message.getText().trim();

        //  Если ожидаем ввод — обрабатываем как данные, не как команду
        if (pendingInputHandlers.containsKey(chatId)) {
            InputHandler handler = pendingInputHandlers.remove(chatId);
            handler.handle(text);
            return;
        }

        if (text.startsWith("/start ")) {
            String payload = text.substring(7).trim(); // "/start " + айди заметки
            if (payload.startsWith("share_")) {
                handleShareLink(payload, message.getFrom().getId(), chatId);
                return;
            }
        }

        //  Определяем логическое имя команды
        String cmdName = null;

        //  Кнопки -> имя команды (то, что возвращает getCommandName())
        if ("❓Помощь".equals(text)) {
            cmdName = "help";
        } else if ("📝 Добавить заметку".equals(text)) {
            cmdName = "addNote";
        } else if ("🔎 Посмотреть все".equals(text)) {
            cmdName = "showNote";
        } else if ("🗑️ Удалить заметку".equals(text)) {
            cmdName = "removeNote";
        }
        // /команды -> извлекаем имя после "/"
        else if (text.startsWith("/")) {
            String[] parts = text.split("\\s+", 2);
            cmdName = parts[0].substring(1); // "/help" -> "help"
        }

        //  Выполняем команду, если найдена
        if (cmdName != null) {
            Command command = CommandRegistry.getCommand(cmdName);
            if (command != null) {
                // Извлекаем аргументы ТОЛЬКО для /команд
                String[] args = new String[0];
                if (text.startsWith("/")) {
                    String[] parts = text.split("\\s+", 2);
                    if (parts.length > 1) {
                        args = parts[1].split("\\s+");
                    }
                }
                command.execute(this, message, args);
            } else {
                sendMessage(chatId, "⚠️ Неизвестная команда. Нажмите ❓Помощь.");
            }
        } else {
            // Не кнопка, не команда
            sendMessage(chatId, "Не понимаю. Используйте кнопки или команды.");
        }
    }
   
    
    public void sendReminder(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    public ReplyKeyboardMarkup createCommandKeyboard() {
    	ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();

        addRow(rows, "❓Помощь", "📝 Добавить заметку");
        addRow(rows, "🔎 Посмотреть все", "🗑️ Удалить заметку");

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);     // маленькая клавиатура
        keyboard.setOneTimeKeyboard(false);   // остаётся после нажатия

        return keyboard;
    }
    
    private void addRow(List<KeyboardRow> rows, String... buttons) {
    	KeyboardRow row = new KeyboardRow();
        row.addAll(Arrays.asList(buttons));
        rows.add(row);
    }
    
    public void sendWithHiddenKeyboard(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        // Убираем клавиатуру
        ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove();
        removeKeyboard.setSelective(true);  // скроет клавиатуру только у текущего пользователя
        msg.setReplyMarkup(removeKeyboard);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
 // ОСНОВНОЙ метод — с клавиатурой по умолчанию
    public void sendMessage(Long chatId, String text) {
        if (text == null) {
            text = "⚠️ Текст заметки отсутствует.";
        }
        sendMessageWithKeyboard(chatId, text, true); // true = показывать клавиатуру
    }

    // Специальный метод — отправить БЕЗ клавиатуры (для /add и тп)
    public void sendMessageWithoutKeyboard(Long chatId, String text) {
        sendMessageWithKeyboard(chatId, text, false);
    }

    // Внутренний вспомогательный метод
    private void sendMessageWithKeyboard(Long chatId, String text, boolean withKeyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);

        if (text == null) {
            text = "⚠️ Текст заметки отсутствует.";
        }

        if (withKeyboard) {
            msg.setReplyMarkup(createCommandKeyboard());
        } else {
            ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
            remove.setSelective(true);
            msg.setReplyMarkup(remove);
        }

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private final SharedNoteRegistry sharedNoteRegistry = new SharedNoteRegistry();

    public SharedNoteRegistry getSharedNoteRegistry() { // для генерации и хранения ссылок
        return sharedNoteRegistry;
    }

    public NoteService getNoteService() { // считка/запись заметок в БД
        return noteService;
    }

    private void handleShareLink(String payload, Long userId, Long chatId) {
        String noteId = payload.substring(6); // отрезаем "share_"

        //удаляем из реестра тк ссылка одноразовая
        SharedNoteData data = sharedNoteRegistry.consume(noteId);
        if (data == null) {
            sendMessage(chatId, "❌ Ссылка недействительна или уже использована.");
            return;
        }

        try {
            // читаем заметку у владельца
            String text = noteService.getNote(data.ownerId, data.noteName);
            if (text == null) {
                sendMessage(chatId, "⚠️ Заметка удалена владельцем.");
                return;
            }

            String newNoteName = "🔖 " + data.noteName;
            noteService.addNoteToDB(userId, newNoteName, text, null);

            sendMessage(chatId,
                    "✅ Получена заметка «" + data.noteName + "»\n" +
                            "Теперь она в вашем списке — можете редактировать как обычную.");

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "❌ Не удалось получить заметку.");
        }
    }
}