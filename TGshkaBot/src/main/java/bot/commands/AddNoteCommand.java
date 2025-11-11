package bot.commands;

import bot.TelegramBot;
import bot.dataBaseService.NoteService;
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
        Long userId = message.getFrom().getId(); // ← КЛЮЧЕВОЕ ИЗМЕНЕНИЕ!

        bot.sendMessage(chatId, "Введите имя добавляемой заметки.");

        bot.setPendingInputHandler(chatId, (noteName) -> {
            if (noteName == null || noteName.trim().isEmpty()) {
                bot.sendMessage(chatId, "Имя заметки не может быть пустым. Попробуйте снова: /addNote");
                return;
            }

            String cleanName = noteName.trim();
            bot.sendMessage(chatId, "Введите текст для заметки \"" + cleanName + "\".");

            bot.setPendingInputHandler(chatId, (noteText) -> {
                if (noteText == null || noteText.trim().isEmpty()) {
                    bot.sendMessage(chatId, "Текст заметки не может быть пустым. Операция отменена.");
                    return;
                }

                try {
                    noteService.addNoteToDB(userId, cleanName, noteText.trim());
                    bot.sendMessage(chatId, "✅ Заметка \"" + cleanName + "\" добавлена!");
                } catch (Exception e) {
                    System.err.println("Ошибка сохранения заметки:");
                    e.printStackTrace();
                    bot.sendMessage(chatId, "❌ Ошибка добавления заметки. Проверьте логи.");
                }
            });
        });
    }
}