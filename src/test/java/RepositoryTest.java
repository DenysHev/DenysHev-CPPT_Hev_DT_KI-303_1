import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class RepositoryTest {

    @Test
    void storesTypedValuesAndReturnsImmutableSnapshot() {
        Repository<SparePartRecord> repository = new Repository<>();
        SparePartRecord record =
                new SparePartRecord("A-1", "Фільтр", 4, 125.50, "Bosch");

        repository.add(record);
        List<SparePartRecord> snapshot = repository.snapshot();

        assertEquals(List.of(record), snapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(record));
        assertEquals(record, repository.find(item -> item.article().equals("A-1"))
                .orElseThrow());
    }

    @Test
    void rejectsNullValuesAndSupportsMissingSearchResult() {
        Repository<String> repository = new Repository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertTrue(repository.find(value -> value.equals("missing")).isEmpty());
    }
}
