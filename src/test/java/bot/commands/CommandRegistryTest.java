package bot.commands;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    @Test
    void shouldRegisterAllCommands() {
        Map<String, Command> allCommands = CommandRegistry.getAllCommands();

        assertTrue(allCommands.containsKey("about"));
        assertTrue(allCommands.containsKey("authors"));
        assertTrue(allCommands.containsKey("help"));

        assertInstanceOf(AboutCommand.class, allCommands.get("about"));
        assertInstanceOf(AuthorsCommand.class, allCommands.get("authors"));
        assertInstanceOf(HelpCommand.class, allCommands.get("help"));
    }

    @Test
    void shouldGetCommandByName() {
        Command about = CommandRegistry.getCommand("about");
        Command unknown = CommandRegistry.getCommand("unknown");

        assertInstanceOf(AboutCommand.class, about);
        assertNull(unknown);
    }

    @Test
    void shouldReturnImmutableCopyOfCommands() {
        Map<String, Command> copy = CommandRegistry.getAllCommands();
        int sizeBefore = copy.size();

        // Пытаемся изменить копию
        copy.put("fake", new AboutCommand());

        // Оригинал не должен измениться
        assertEquals(sizeBefore, CommandRegistry.getAllCommands().size());
    }
}