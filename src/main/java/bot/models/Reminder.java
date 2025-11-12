package bot.models;

import java.time.LocalDateTime;

public class Reminder {
    public enum Type {
        SINGLE, DAILY, WEEKLY, INTERVAL, REPEATING
    }

    private int id;
    private int groupId;
    private String content;
    private Type type;
    private LocalDateTime triggerTime;      // для SINGLE
    private Integer intervalMinutes;         // для INTERVAL / REPEATING
    private String daysOfWeek;              // "Mon,Tue" или JSON
    private boolean enabled;
    private LocalDateTime createdAt;

    // Конструкторы
    public Reminder() {}

    public Reminder(int groupId, String content, Type type) {
        this.groupId = groupId;
        this.content = content;
        this.type = type;
        this.enabled = true;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public LocalDateTime getTriggerTime() { return triggerTime; }
    public void setTriggerTime(LocalDateTime triggerTime) { this.triggerTime = triggerTime; }

    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", groupId=" + groupId +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", triggerTime=" + triggerTime +
                ", intervalMinutes=" + intervalMinutes +
                ", daysOfWeek='" + daysOfWeek + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}