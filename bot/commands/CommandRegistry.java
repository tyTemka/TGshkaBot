package bot.commands;

import java.util.HashMap;
import java.util.Map;
import bot.commands.*;

public class CommandRegistry {
    private static final Map<String, Commands> COMMANDS = new HashMap<>();

    static {
        // Регистрируем команды
        AboutCommand about = new AboutCommand();
        AuthorsCommand authors = new AuthorsCommand();

        COMMANDS.put(about.getCommandName(), about);
        COMMANDS.put(authors.getCommandName(), authors);

        // Help-команда должна быть зарегистрирована ПОСЛЕДНЕЙ, чтобы видеть все команды
        HelpCommand help = new HelpCommand(COMMANDS);
        COMMANDS.put(help.getCommandName(), help);
    }

    public static Commands getCommand(String commandName) {
        return COMMANDS.get(commandName);
    }

    public static Map<String, Commands> getAllCommands() {
        return new HashMap<>(COMMANDS); // защищаем от модификации извне
    }
}