package bot.utilits;

import bot.models.SharedNoteData;
import java.util.concurrent.ConcurrentHashMap;

public class SharedNoteRegistry {
    private final ConcurrentHashMap<String, SharedNoteData> map = new ConcurrentHashMap<>();

    // Генерируем ID и сохраняем данные
    public String register(long ownerId, String noteName) {
        String id = "note_" + System.currentTimeMillis();
        map.put(id, new SharedNoteData(ownerId, noteName));
        return id;
    }

    // Получаем и удаляем (ссылка одноразовая)
    public SharedNoteData consume(String id) {
        return map.remove(id);
    }
}
