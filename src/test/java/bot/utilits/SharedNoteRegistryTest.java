package bot.utilits;

import bot.models.SharedNoteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SharedNoteRegistryTest {

    private SharedNoteRegistry registry; //создаем экземпляр тестируемого класса

    @BeforeEach
    // обновляем для тестов
    void setUp() {
        registry = new SharedNoteRegistry();
    }

    @Test
    // Проверка добавления в реестр
    // Ожидаем: новая запись запишется в реестр и доступна по айди
    void register_addsEntryToRegistry() {
        // После вызова register запись должна появиться в реестре
        String id = registry.register(100L, "Test");
        assertTrue(registry.contains(id));
    }

    @Test
    // Проверка нескольких записей
    // Ожидаем: в реестр добавятся несколько записей
    void size_increasesAfterRegister() {
        assertEquals(0, registry.size());
        registry.register(1L, "A");
        registry.register(2L, "B");
        assertEquals(2, registry.size());
    }

    @Test
    // Проверка одноразовости ссылок
    // Ожидаем: запись вернется и удалится
    void consume_removesEntryAndReturnsData() {
        String id = registry.register(1L, "Note");
        SharedNoteData data = registry.consume(id);

        assertNotNull(data);
        assertEquals(1L, data.ownerId);
        assertEquals("Note", data.noteName);
        assertFalse(registry.contains(id)); // больше не существует
        assertEquals(0, registry.size());
    }

    @Test
    // Тестинг старых и поддельных ссылок
    // Ожидаем: null
    void consume_returnsNullForUnknownId() {
        // Для несуществующего ID должен возвращаться null
        assertNull(registry.consume("invalid_id"));
    }
}