package bot.commands;

import bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ShareCommand implements Command {
    @Override
    public String getCommandName() {
        return "share";
    }
    @Override
    public String getDescription() {
        return "Поделиться заметкой по ссылке";
    }

    @Override
    public String getUsage() {
        return "/share <имя_заметки>";
    }

    @Override
    public void execute(TelegramBot bot, Message msg, String[] args) {
        Long chatId = msg.getChatId();
        Long userId = msg.getFrom().getId();

        if (args.length == 0) {
            bot.sendMessage(chatId, "📌 Укажите имя заметки.\nПример: /share Список_дел");
            return;
        }

        String noteName = args[0];

        try {
            // Проверяем: есть ли такая заметка у пользователя?
            if (!bot.getNoteService().noteExists(userId, noteName)) {
                bot.sendMessage(chatId, "❌ Заметка «" + noteName + "» не найдена.");
                return;
            }

            // Генерируем ID и сохраняем в реестре
            String noteId = bot.getSharedNoteRegistry().register(userId, noteName);

            // Формируем ссылку
            String link = "https://t.me/OOPOOP_tg_bot?start=share_" + noteId;

            bot.sendMessage(chatId, "🔗 Ссылка для заметки «" + noteName + "»:\n" + link + "\n\n" +
                            "Отправьте её другу — после перехода он получит свою копию.");
        } catch (Exception e) {
            bot.sendMessage(chatId, "❌ Ошибка при создании ссылки.");
            e.printStackTrace();
        }
    }

}
