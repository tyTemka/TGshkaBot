package bot.models;

import java.time.LocalDateTime;

public class Group {
    private int id;
    private String name;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;

    // Конструкторы
    public Group() {}

    public Group(String name, String description) {
        this.name = name;
        this.description = description;
        this.enabled = true;
    }

    public Group(int id, String name, String description, boolean enabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}