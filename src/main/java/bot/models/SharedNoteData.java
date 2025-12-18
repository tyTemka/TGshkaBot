package bot.models;

public class SharedNoteData {
    public final long ownerId;
    public final String noteName;

    public SharedNoteData(long ownerId, String noteName) {
        this.ownerId = ownerId;
        this.noteName = noteName;
    }
}