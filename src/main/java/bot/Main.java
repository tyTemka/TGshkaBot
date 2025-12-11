package bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import bot.models.Reminder;
import bot.dataBaseService.*;



public class Main {
    public static void main(String[] args) {
        try {
            TelegramBot bot = new TelegramBot(); //создаем один экземпляр бота 

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class); //подключаем к серверу тг
            telegramBotsApi.registerBot(bot);
            System.out.println("Бот запущен!");

            NoteService noteService = new NoteService(); //сервис для работы с БД
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); //выделяем один поток 

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    System.out.println("Планировщик проверяет напоминания...");
                    List<Reminder> dueReminders = noteService.getDueReminders();
                    
                    for (Reminder r : dueReminders) {
                        // Получаем куда присылать оповещение
                        long chatId = r.getGroupId();   
                        
                        // Отправляем сообщение
                        String message = "🔔 Напоминание:\n" + r.getContent();
                        bot.sendMessage(chatId, message);  // 

                        // убираем напоминание из очереди
                       noteService.clearReminderTime(r.getId());

                        System.out.println("Отправлено напоминание пользователю " + chatId + 
                                         " для заметки ID=" + r.getId());
                    }

                    if (dueReminders.isEmpty()) {
                        System.out.println("Нет активных напоминаний."); //если в очереди нет напоминаний, то выводим...
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка в планировщике напоминаний:"); //обработка эксепшн
                    e.printStackTrace();
                }
            }, 0, 60, TimeUnit.SECONDS); //интервал через сколько проверяем напоминания

        } catch (TelegramApiException e) {
            System.err.println("Ошибка при запуске бота: " + e.getMessage()); //если бот не запустился
            e.printStackTrace();
        }
    }
}


