package bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import bot.models.NoteReminder;
import bot.dataBaseService.*;



public class Main {
    public static void main(String[] args) {
        try {
            TelegramBot bot = new TelegramBot(); // один экземпляр

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            System.out.println("Бот запущен!");

            NoteService noteService = new NoteService();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    System.out.println("Планировщик проверяет напоминания...");
                    List<NoteReminder> dueNotes = noteService.getDueReminders();
                    for (NoteReminder note : dueNotes) {
                        bot.sendReminder(note.userId, "🔔 Напоминание: " + note.text);
                        // здесь можно удалить или обновить remind_at
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 30, TimeUnit.SECONDS);

        } catch (TelegramApiException e) {
            System.err.println("Ошибка при запуске бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


