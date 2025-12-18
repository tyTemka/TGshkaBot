package bot.dataBaseService;

import bot.models.Reminder;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ReminderServiceTest {

    @Test
    void shouldAddAndRetrieveReminder() throws SQLException {
        ReminderService service = new ReminderService(); // in-memory

        Reminder r = new Reminder(123, "Тест", Reminder.Type.SINGLE);
        r.setTriggerTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        service.add(r);

        List<Reminder> list = service.getByUser(123);
        assertThat(list).hasSize(1);

        Reminder saved = list.get(0);
        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getGroupId()).isEqualTo(123);
        assertThat(saved.getContent()).isEqualTo("Тест");
        assertThat(saved.getType()).isEqualTo(Reminder.Type.SINGLE);
        assertThat(saved.getTriggerTime()).isEqualTo(r.getTriggerTime());
        assertThat(saved.getIntervalMinutes()).isNull();
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void shouldFindDueReminders() throws SQLException {
        ReminderService service = new ReminderService();
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);

        Reminder due = new Reminder(123, "Сейчас", Reminder.Type.SINGLE);
        due.setTriggerTime(now.plusSeconds(30));
        service.add(due);

        Reminder future = new Reminder(123, "Позже", Reminder.Type.SINGLE);
        future.setTriggerTime(now.plusMinutes(10));
        service.add(future);

        List<Reminder> dueList = service.getDueBefore(now.plusMinutes(1));
        assertThat(dueList).hasSize(1);
        assertThat(dueList.get(0).getContent()).isEqualTo("Сейчас");
    }

    @Test
    void shouldHandleIntervalMinutesAsNull() throws SQLException {
        ReminderService service = new ReminderService();

        Reminder r = new Reminder(123, "Без интервала", Reminder.Type.SINGLE);
        service.add(r); // intervalMinutes = null

        List<Reminder> list = service.getByUser(123);
        assertThat(list.get(0).getIntervalMinutes()).isNull();
    }
}