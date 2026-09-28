import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class TaskMetricsTest {

    private final TaskMetrics metrics = new TaskMetrics();

    @Test
    void emptyListYieldsZeroTotalsAndNull() {
        assertEquals(0, metrics.totalStock(List.of()));
        assertEquals(0.0, metrics.totalInventoryValue(List.of()), 0.001);
        assertNull(metrics.mostExpensivePart(List.of()));
    }

    @Test
    void singleTask() {
        Task t = new Task("SKU001", "Колодка", 3, 100.00, "Bosch");
        List<Task> tasks = List.of(t);

        assertEquals(3, metrics.totalStock(tasks));
        assertEquals(300.00, metrics.totalInventoryValue(tasks), 0.001);
        assertEquals(t, metrics.mostExpensivePart(tasks));
    }

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
    void shouldFindMostExpensivePart() {
        Task cheap = new Task("SKU001", "Колодки", 10, 100.00, "Bosch");
        Task expensive = new Task("SKU002", "Амортизатор", 5, 2500.00, "Sachs");

        Task result = metrics.mostExpensivePart(List.of(cheap, expensive));

        assertNotNull(result);
        assertEquals("SKU002", result.sku());
        assertEquals("Амортизатор", result.name());
        assertEquals(2500.00, result.unitPrice(), 0.001);
    }

    @Test
    void shouldCalculateTotalInventoryValue() {
        List<Task> tasks = List.of(
                new Task("SKU001", "Колодки", 10, 100.00, "Bosch"),
                new Task("SKU002", "Фільтр", 5, 200.00, "Mann"),
                new Task("SKU003", "Дорогий", 1, 999.99, "Test")
        );

        assertEquals(2999.99, metrics.totalInventoryValue(tasks), 0.001);
    }

    @Test
    void zeroStockAndZeroPriceAreHandled() {
        List<Task> tasks = List.of(
                new Task("SKU001", "Без ціни", 0, 0.0, "Test"),
                new Task("SKU002", "Без залишку", 10, 0.0, "Test")
        );

        assertEquals(10, metrics.totalStock(tasks));
        assertEquals(0.0, metrics.totalInventoryValue(tasks), 0.001);
        assertNotNull(metrics.mostExpensivePart(tasks));
    }
}