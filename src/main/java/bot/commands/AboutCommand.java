package bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class AboutCommand implements Commands {
    @Override
    public String getCommandName() {
        return "about";
    }

    @Override
    public String getDescription() {
        return "Показать информацию о боте";
    }

    @Override
    public String getUsage() {
        return "/about";
    }

    @Override
    public void execute(AbsSender sender, Message message, String[] args) {
        SendMessage msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setText("Это бот для заметок. Версия 0.1.");
        try {
            sender.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}