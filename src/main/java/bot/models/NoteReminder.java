package bot.models;

public class NoteReminder {
    public Long userId;
    public String noteName;
    public String text;

    public NoteReminder(Long userId, String noteName, String text) {
        this.userId = userId;
        this.noteName = noteName;
        this.text = text;
    }
}
