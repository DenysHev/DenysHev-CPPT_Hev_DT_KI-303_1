import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

class TaskMetricsTest {

    private final TaskMetrics metrics = new TaskMetrics();

    @Test
    void emptyListYieldsZeroTotalsAndNull() {
        StockValue result = metrics.calculate(List.of());

        assertEquals(0, result.validRecords());
        assertEquals(0, result.totalStock());
        assertEquals(0.0, result.totalInventoryValue(), 0.001);
        assertNull(result.mostExpensivePart());
    }

    @Test
    void singleTask() {
        SparePart t = new SparePart("SKU001", "Колодка", 3, 100.00, "Bosch");
        List<SparePart> tasks = List.of(t);

        StockValue result = metrics.calculate(tasks);

        assertEquals(1, result.validRecords());
        assertEquals(3, result.totalStock());
        assertEquals(300.00, result.totalInventoryValue(), 0.001);
        assertEquals(t, result.mostExpensivePart());
    }

    @Test
    void shouldCalculateTotalStock() {
        List<SparePart> tasks = List.of(
                new SparePart("SKU001", "Колодки", 10, 850.50, "Bosch"),
                new SparePart("SKU002", "Фільтр", 20, 320.00, "Mann"),
                new SparePart("SKU003", "Амортизатор", 5, 2450.00, "Sachs")
        );
        assertEquals(35, metrics.calculate(tasks).totalStock());
    }

    @Test
    void shouldFindMostExpensivePart() {
        SparePart cheap = new SparePart("SKU001", "Колодки", 10, 100.00, "Bosch");
        SparePart expensive = new SparePart("SKU002", "Амортизатор", 5, 2500.00, "Sachs");

        SparePart result = metrics.calculate(List.of(cheap, expensive))
                .mostExpensivePart();

        assertNotNull(result);
        assertEquals("SKU002", result.sku());
        assertEquals("Амортизатор", result.name());
        assertEquals(2500.00, result.unitPrice(), 0.001);
    }

    @Test
    void shouldCalculateTotalInventoryValue() {
        List<SparePart> tasks = List.of(
                new SparePart("SKU001", "Колодки", 10, 100.00, "Bosch"),
                new SparePart("SKU002", "Фільтр", 5, 200.00, "Mann"),
                new SparePart("SKU003", "Дорогий", 1, 999.99, "Test")
        );

        assertEquals(
                2999.99,
                metrics.calculate(tasks).totalInventoryValue(),
                0.001
        );
    }

    @Test
    void zeroStockAndZeroPriceAreHandled() {
        List<SparePart> tasks = List.of(
                new SparePart("SKU001", "Без ціни", 0, 0.0, "Test"),
                new SparePart("SKU002", "Без залишку", 10, 0.0, "Test")
        );

        StockValue result = metrics.calculate(tasks);

        assertEquals(2, result.validRecords());
        assertEquals(10, result.totalStock());
        assertEquals(0.0, result.totalInventoryValue(), 0.001);
        assertNotNull(result.mostExpensivePart());
    }

    @Test
    void inventoryMetricsRecordExposesImmutableSummary() {
        SparePart part = new SparePart(
                "SKU001", "Колодка", 3, 100.00, "Bosch"
        );
        StockValue first = new StockValue(1, 3, 300.00, part);
        StockValue second = new StockValue(1, 3, 300.00, part);

        assertEquals(1, first.validRecords());
        assertEquals(3, first.totalStock());
        assertEquals(300.00, first.totalInventoryValue());
        assertSame(part, first.mostExpensivePart());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}