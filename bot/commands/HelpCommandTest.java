package bot.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HelpCommandTest {
    @Mock
    private AbsSender sender;

    @Mock
    private Message message;

    @Captor
    private ArgumentCaptor<SendMessage> sendMessageCaptor;

    private HelpCommand helpCommand;

    @BeforEach
    void setUp(){
        Commands helpCmd = mock(Commands.class);
        when(helpCmd.getCommandName()).thenReturn("help");
        when(helpCmd.getDescription()).thenReturn("Показать помощь");
        when(helpCmd.getUsage()).thenReturn("/help [команда]");

        Commands aboutCmd = mock(Commands.class);
        when(aboutCmd.getCommandName()).thenReturn("about");
        when(aboutCmd.getDescription()).thenReturn("Информация о боте");
        when(aboutCmd.getUsage()).thenReturn("/about");

        Commands authorsCmd = mock(Commands.class);
        when(authorsCmd.getCommandName()).thenReturn("authors");
        when(authorsCmd.getDescription()).thenReturn("Информация об авторах");
        when(aboutCmd.getUsage()).thenReturn("/authors");

        Map<String, Commands> registry = Map.of(
                "help", helpCmd,
                "about", aboutCmd,
                "authors", authorsCmd
        );

        helpCommand = new HelpCommand(registry);

    }

    @Test
    void shouldRetFull_ListCmd_withoutArg() throws TelegramApiException{
        // Given
        long chatId = 12345L;
        when(message.getChatId()).thenReturn(chatId);
        // When
        helpCommand.execute(sender, message, new String[]{});
        // Then
        verify(sender).execute(sendMessageCaptor.capture());
        SendMessage sent = sendMessageCaptor.getValue();

        assertThat(sent.getChatId()).isEqualTo(String.valueOf(chatId));
        String text = sent.getText();

        assertThat(text).startsWith("Доступные команды:");
        assertThat(text).contains("/help — Показать помощь по командам");
        assertThat(text).contains("/about — Информация о боте");
        assertThat(text).contains("/authors — Авторы бота");
    }

    @Test
    void shouldRetFull_ListCmd_withAbout() throws TelegramApiException{
        long chatId = 12345L;
        //Given
        when(message.getChatId()).thenReturn(String.valueOf(chatId));
        //When
        helpCommand.execute(sender, message, new String[]{"about"});
        //Then
        verify(sender).execute(sendMessageCaptor.capture());
        SendMessage sent = sendMessageCaptor.getValue();

        assertThat(sent.getChatId()).isEqualTo(String.valueOf(chatId));
        String text = sent.getText();

        assertThat(text).contains("Команда: /about");
        assertThat(text).contains("Описание: Информация о боте");
        assertThat(text).contains("Использование: /about");

    }

    @Test
    void shouldRetFull_ListCmd_withAuthors() throws TelegramApiException{
        long chatId = 12345L;
        //Given
        when(message.getChatId()).thenReturn(String.valueOf(chatId));
        //When
        helpCommand.execute(sender, message, new String[]{"authors"});
        //Then
        verify(sender).execute(sendMessageCaptor.capture());
        SendMessage sent = sendMessageCaptor.getValue();

        assertThat(sent.getChatId()).isEqualTo(String.valueOf(chatId));
        String text = sent.getText();

        assertThat(text).contains("Команда: /authors");
        assertThat(text).contains("Описание: Информация об авторах");
        assertThat(text).contains("Использование: /authors");
    }

    @Test
    void shouldRetFull_ListCmd_withHelp() throws TelegramApiException{
        long chatId = 12345L;
        //Given
        when(message.getChatId()).thenReturn(String.valueOf(chatId));
        //When
        helpCommand.execute(sender, message, new String[]{"help"});
        //Then
        verify(sender).execute(sendMessageCaptor.capture());
        SendMessage sent = sendMessageCaptor.getValue();

        assertThat(sent.getChatId()).isEqualTo(String.valueOf(chatId));
        String text = sent.getText();

        assertThat(text).contains("Команда: /help");
        assertThat(text).contains("Описание: Помощь");
        assertThat(text).contains("Использование: /help");
    }

    @Test
    void shouldRetFull_ListCmd_withUnknownCmd() throws TelegramApiException{
        long chatId = 12345L;
        //Given
        when(message.getChatId()).thenReturn(String.valueOf(chatId));
        //When
        helpCommand.execute(sender, message, new String[]{"unknown"});
        // Then
        verify(sender).execute(sendMessageCaptor.capture());
        SendMessage sent = sendMessageCaptor.getValue();

        assertThat(sent.getChatId()).isEqualTo(String.valueOf(chatId));
        assertThat(sent.getText()).isEqualTo("Команда 'unknown' не найдена.");
    }

    @Test
    void shouldRetFull_ListCmd_withUnknownCmd() throws TelegramApiException{
        long chatId = 12345L;
        //Given
        when(message.getChatId()).thenReturn(String.valueOf(chatId));
        //When
        helpCommand.execute(sender, message, new String[]{""});
        // Then
        verify(sender).execute(sendMessageCaptor.capture());
        String text = sendMessageCaptor.getValue().getText();

        assertThat(text).startsWith("Доступные команды:");
        assertThat(text).contains("/about");
        assertThat(text).contains("/authors");
    }

}
