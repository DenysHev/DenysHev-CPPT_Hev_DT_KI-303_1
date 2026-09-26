import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskMetricsTest {

    private final TaskMetrics metrics = new TaskMetrics();

    @Test
    void shouldCalculateTotalStock() {
        List<Task> tasks = List.of(
                new Task("SKU001", "Колодки", 10, 850.50, "Bosch"),
                new Task("SKU002", "Фільтр", 20, 320.00, "Mann"),
                new Task("SKU003", "Амортизатор", 5, 2450.00, "Sachs")
        );

        assertEquals(35, metrics.totalStock(tasks));
    }

    @Test
    void shouldCalculateTotalInventoryValue() {
        List<Task> tasks = List.of(
                new Task("SKU001", "Колодки", 10, 100.00, "Bosch"),
                new Task("SKU002", "Фільтр", 5, 200.00, "Mann")
        );

        assertEquals(2000.00, metrics.totalInventoryValue(tasks), 0.001);
    }

    @Test
    void shouldFindMostExpensivePart() {
        Task cheap = new Task(
                "SKU001", "Колодки", 10, 100.00, "Bosch"
        );

        Task expensive = new Task(
                "SKU002", "Амортизатор", 5, 2500.00, "Sachs"
        );

        List<Task> tasks = List.of(cheap, expensive);

        Task result = metrics.mostExpensivePart(tasks);

        assertNotNull(result);
        assertEquals("Амортизатор", result.name());
        assertEquals(2500.00, result.unitPrice(), 0.001);
    }
}
