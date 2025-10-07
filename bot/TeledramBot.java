package bot;

import bot.commands.CommandRegistry;
import bot.commands.Commands;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;

public class TeledramBot extends TelegramLongPollingBot {

    private final String botToken;

    public TeledramBot(String botToken) {
        this.botToken = botToken;
    }

    @Override
    public String getBotUsername() {
        return "Unterrichtung_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText().trim();

                if (text.startsWith("/")) {
                    // Парсим команду и аргументы
                    String[] parts = text.split("\\s+", 2);
                    String cmdName = parts[0].substring(1); // убираем "/"
                    String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[0];

                    Commands command = CommandRegistry.getCommand(cmdName);
                    if (command != null) {
                        command.execute(this, message, args);
                    } else {
                        sendMessage(message.getChatId(), "Неизвестная команда. Введите /help для списка.");
                    }
                }
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        org.telegram.telegrambots.meta.api.methods.send.SendMessage msg =
            new org.telegram.telegrambots.meta.api.methods.send.SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        try {
            execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}