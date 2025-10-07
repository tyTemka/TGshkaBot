package bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class AuthorsCommand implements Commands {
    @Override
    public String getCommandName() {
        return "authors";
    }

    @Override
    public String getDescription() {
        return "Показать авторов бота";
    }

    @Override
    public String getUsage() {
        return "/authors";
    }

    @Override
    public void execute(AbsSender sender, Message message, String[] args) {
        SendMessage msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setText("Авторы:Биба Екимов и Боба Василенко");
        try {
            sender.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}