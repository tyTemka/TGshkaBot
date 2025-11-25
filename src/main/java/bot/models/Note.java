package bot.models;

import java.time.LocalDateTime;

public class Note {
    private int id;
    private int groupId;
    private String content;
    private LocalDateTime createdAt;

    public Note() {}

    public Note(int groupId, String content) {
        this.groupId = groupId;
        this.content = content;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}