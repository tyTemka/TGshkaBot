package bot.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // ← для объявления поля
import org.mockito.Captor; // ← для аннотации @Captor
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*; // Чтобы не писать Mokito.when

@ExtendWith(MockitoExtension.class)
public class AboutCommandTest {
	@Mock
	private AbsSender sender;
	
	@Mock
	private Message message;
	
    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor; // Ловим аргументы из SendMessage

    private final AboutCommand command = new AboutCommand();	
	
	@Test											// для verify
	void shouldSendAboutMessageToCorrectChat() throws TelegramApiException {
		// Given
		Long chatId = 123456789L;
        when(message.getChatId()).thenReturn(chatId);
        // When
        command.execute(sender, message, new String[]{});
        // Then
        verify(sender).execute(sendMessageCaptor.capture());

        SendMessage sendMessage = sendMessageCaptor.getValue();
        assertThat(sendMessage.getChatId()).isEqualTo(chatId.toString());
        assertThat(sendMessage.getText()).isEqualTo("Это бот для заметок. Версия 0.1.");
	}
    @Test
    void shouldHandleEmptyArgs() throws TelegramApiException {
        // Given
        when(message.getChatId()).thenReturn(987654321L);

        // When
        command.execute(sender, message, new String[]{}); // пустой массив

        // Then
        verify(sender).execute(any(SendMessage.class));
    }

    @Test
    void shouldHandleNonEmptyArgs() throws TelegramApiException {
        // Given
        when(message.getChatId()).thenReturn(111222333L);

        // When
        command.execute(sender, message, new String[]{"лишний", "аргумент"});

        // Then
        verify(sender).execute(any(SendMessage.class));
    }
}
