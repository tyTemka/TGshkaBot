package bot.commands;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;

import bot.TelegramBot;

@ExtendWith(MockitoExtension.class)
public class AboutCommandTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private Message message;

    private final AboutCommand command = new AboutCommand();

    @Test
    void shouldSendAboutMessageToCorrectChat() {
        // Given
        Long chatId = 123456789L;
        when(message.getChatId()).thenReturn(chatId);

        // When
        command.execute(bot, message, new String[]{});

        // Then
        verify(bot).sendMessage(eq(chatId), eq("Это бот для заметок. Версия 0.1."));
    }

    @Test
    void shouldHandleEmptyArgs() {
        when(message.getChatId()).thenReturn(987654321L);
        command.execute(bot, message, new String[]{});
        verify(bot).sendMessage(anyLong(), anyString());
    }

    @Test
    void shouldHandleNonEmptyArgs() {
        when(message.getChatId()).thenReturn(111222333L);
        command.execute(bot, message, new String[]{"лишний", "аргумент"});
        verify(bot).sendMessage(anyLong(), anyString());
    }
}