package bot.commands;

import bot.TelegramBot;
import bot.dataBaseService.NoteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.telegram.telegrambots.meta.api.objects.Message;

public class AddNoteCommand implements Command {

    @Override
    public String getCommandName() {
        return "addNote";
    }

    @Override
    public String getDescription() {
        return "Добавить заметку";
    }

    @Override
    public String getUsage() {
        return "/addNote";
    }

    private final NoteService noteService = new NoteService();

    @Override
    public void execute(TelegramBot bot, Message message, String[] args) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        bot.sendMessage(chatId, "📝 Введите имя добавляемой заметки.");

        bot.setPendingInputHandler(chatId, (noteName) -> {
            if (noteName == null || noteName.trim().isEmpty()) {
                bot.sendMessage(chatId, "❗ Имя заметки не может быть пустым. Попробуйте снова: /addNote");
                return;
            }
            String cleanName = noteName.trim();

            bot.sendMessage(chatId, "✍️ Введите текст для заметки \"" + cleanName + "\".");

            bot.setPendingInputHandler(chatId, (noteText) -> {
                if (noteText == null || noteText.trim().isEmpty()) {
                    bot.sendMessage(chatId, "❗ Текст заметки не может быть пустым. Операция отменена.");
                    return;
                }
                String cleanText = noteText.trim();

                askForReminderDate(bot, chatId, userId, cleanName, cleanText);
            });
        });
    }
    
    private void askForReminderDate(
    	    TelegramBot bot, Long chatId, Long userId,
    	    String cleanName, String cleanText
    	) {
    	    bot.sendMessage(chatId,
    	        "⏰ Введите время напоминания (формат: 'yyyy-MM-dd HH:mm'),\n" +
    	        "или напишите 'нет':");
    	    
    	    bot.setPendingInputHandler(chatId, (timeInput) -> {
    	        if (timeInput.equalsIgnoreCase("нет")) {
    	            // Сохраняем без напоминания
    	            saveAndRespond(bot, chatId, userId, cleanName, cleanText, null);
    	            return;
    	        }

    	        try {
    	            LocalDateTime remindAt = LocalDateTime.parse(
    	                timeInput.trim(),
    	                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    	            );
    	            saveAndRespond(bot, chatId, userId, cleanName, cleanText, remindAt);
    	        } catch (Exception e) {
    	            // ❗ ОШИБКА → просто переспрашиваем, НЕ выходя из процесса
    	            bot.sendMessage(chatId,
    	                "⚠️ Неверный формат. Попробуйте ещё раз:\n" +
    	                "Пример: '2025-12-10 14:30' или 'нет'");
    	            
    	            askForReminderDate(bot, chatId, userId, cleanName, cleanText);
    	        }
    	    });
    	}

    	private void saveAndRespond(
    	    TelegramBot bot, Long chatId, Long userId,
    	    String name, String text, LocalDateTime remindAt
    	) {
    	    try {
    	        noteService.addNoteToDB(userId, name, text, remindAt);
    	        String msg = "✅ Заметка «" + name + "» добавлена";
    	        if (remindAt != null) {
    	            msg += " с напоминанием на " +
    	                   remindAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    	        } else {
    	            msg += " без напоминания.";
    	        }
    	        bot.sendMessage(chatId, msg);
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        bot.sendMessage(chatId, "❌ Ошибка при сохранении. Попробуйте позже.");
    	    }
    	}
}