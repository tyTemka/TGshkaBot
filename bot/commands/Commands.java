package bot.commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface Commands {
 String getCommandName();       // например: "help"
 String getDescription();       // краткое описание
 String getUsage();             // как использовать (например: "/help <command>")
 void execute(AbsSender sender, Message message, String[] args);
}