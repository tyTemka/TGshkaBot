package bot.commands;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    @Test
    void shouldRegisterAllCommands() {
        Map<String, Commands> allCommands = CommandRegistry.getAllCommands();

        assertEquals(3, allCommands.size());
        assertTrue(allCommands.containsKey("about"));
        assertTrue(allCommands.containsKey("authors"));
        assertTrue(allCommands.containsKey("help"));

        assertInstanceOf(AboutCommand.class, allCommands.get("about"));
        assertInstanceOf(AuthorsCommand.class, allCommands.get("authors"));
        assertInstanceOf(HelpCommand.class, allCommands.get("help"));
    }

    @Test
    void shouldGetCommandByName() {
        Commands about = CommandRegistry.getCommand("about");
        Commands unknown = CommandRegistry.getCommand("unknown");

        assertInstanceOf(AboutCommand.class, about);
        assertNull(unknown);
    }

    @Test
    void shouldReturnImmutableCopyOfCommands() {
        Map<String, Commands> copy = CommandRegistry.getAllCommands();
        int sizeBefore = copy.size();

        // Пытаемся изменить копию
        copy.put("fake", new AboutCommand());

        // Оригинал не должен измениться
        assertEquals(sizeBefore, CommandRegistry.getAllCommands().size());
    }
}