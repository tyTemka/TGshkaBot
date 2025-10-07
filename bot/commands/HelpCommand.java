package bot.commands;

import java.util.Map;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

public class HelpCommand implements Commands {
    private final Map<String, Commands> commandRegistry;

    public HelpCommand(Map<String, Commands> commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Показать помощь по командам";
    }

    @Override
    public String getUsage() {
        return "/help [команда]";
    }

    @Override
    public void execute(AbsSender sender, Message message, String[] args) {
        StringBuilder response = new StringBuilder();

        if (args.length > 0 && !args[0].isEmpty()) {
            // Запрос помощи по конкретной команде
            Commands cmd = commandRegistry.get(args[0]);
            if (cmd != null) {
                response.append("Команда: /").append(cmd.getCommandName()).append("\n")
                        .append("Описание: ").append(cmd.getDescription()).append("\n")
                        .append("Использование: ").append(cmd.getUsage());
            } else {
                response.append("Команда '").append(args[0]).append("' не найдена.");
            }
        } else {
            // Общий список команд
            response.append("Доступные команды:\n");
            for (Commands cmd : commandRegistry.values()) {
                response.append("/").append(cmd.getCommandName())
                        .append(" — ").append(cmd.getDescription()).append("\n");
            }
        }

        SendMessage msg = new SendMessage();
        msg.setChatId(message.getChatId());
        msg.setText(response.toString());
        try {
            sender.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}