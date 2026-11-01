import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class InventoryStreamQueriesTest {

    private final InventoryStreamQueries queries =
            new InventoryStreamQueries();

    private final List<SparePart> parts = List.of(
            new OriginalPart("SKU-1", "Стабілізатор", 10, 100.00, "Bosch"),
            new ReplacementPart("REP-2", "Амортизатор", 3, 500.00, "Mann"),
            new OriginalPart("SKU-3", "Колодка", 5, 500.00, "Bosch"),
            new OriginalPart("SKU-4", "Фільтр", 10, 50.00, "Mann"),
            new OriginalPart("SKU-1", "Дубль", 2, 75.00, "Bosch"),
            new ReplacementPart("REP-6", "Свічка", 20, 25.00, "NGK"));

    @Test
    void filtersOnlyStockBelowThreshold() {
        assertEquals(
                List.of("REP-2", "SKU-3", "SKU-1"),
                queries.filterLowStock(parts, 10).stream()
                        .map(SparePart::sku)
                        .toList());
    }

    @Test
    void emptyInputProducesEmptyQueryResults() {
        assertTrue(queries.filterLowStock(List.of(), 10).isEmpty());
        assertTrue(queries.mapSkus(List.of()).isEmpty());
        assertTrue(queries.inventoryValueBySupplier(List.of()).isEmpty());
        assertTrue(queries.topByPrice(List.of(), 5).isEmpty());
        assertTrue(queries.findBySku(List.of(), "SKU-1").isEmpty());
    }

    @Test
    void queriesDoNotMutateInputOrderOrContents() {
        List<SparePart> original = List.copyOf(parts);

        queries.filterLowStock(parts, 10);
        queries.mapSkus(parts);
        queries.inventoryValueBySupplier(parts);
        queries.priceStatistics(parts);
        queries.topByPrice(parts, 5);
        queries.findBySku(parts, "SKU-1");

        assertEquals(original, parts);
    }

    @Test
    void mapsSkusWithoutChangingInput() {
        assertEquals(
                List.of("SKU-1", "REP-2", "SKU-3", "SKU-4", "SKU-1", "REP-6"),
                queries.mapSkus(parts));
        assertEquals("SKU-1", parts.get(0).sku());
    }

    @Test
    void groupsInventoryValueBySupplier() {
        Map<String, Double> result =
                queries.inventoryValueBySupplier(parts);

        assertEquals(3_650.00, result.get("Bosch"), 0.001);
        assertEquals(2_000.00, result.get("Mann"), 0.001);
        assertEquals(500.00, result.get("NGK"), 0.001);
    }

    @Test
    void summarizesPrices() {
        var result = queries.priceStatistics(parts);

        assertEquals(6, result.getCount());
        assertEquals(1_250.00, result.getSum(), 0.001);
        assertEquals(25.00, result.getMin(), 0.001);
        assertEquals(500.00, result.getMax(), 0.001);
        assertEquals(208.333, result.getAverage(), 0.001);
    }

    @Test
    void emptyPriceStatisticsHaveZeroCountAndSum() {
        var result = queries.priceStatistics(List.of());

        assertEquals(0, result.getCount());
        assertEquals(0.0, result.getSum(), 0.001);
    }

    @Test
    void sortsTopByPriceAndUsesNameAsTieBreaker() {
        assertEquals(
                List.of("Амортизатор", "Колодка", "Стабілізатор"),
                queries.topByPrice(parts, 3).stream()
                        .map(SparePart::name)
                        .toList());
    }

    @Test
    void topHandlesZeroLargeAndNegativeLimits() {
        assertTrue(queries.topByPrice(parts, 0).isEmpty());
        assertEquals(6, queries.topByPrice(parts, 50).size());
        assertThrows(
                IllegalArgumentException.class,
                () -> queries.topByPrice(parts, -1));
    }

    @Test
    void findsFirstDuplicateAndReturnsEmptyForMissingSku() {
        assertEquals("Стабілізатор",
                queries.findBySku(parts, "SKU-1").orElseThrow().name());
        assertFalse(queries.findBySku(parts, "ABSENT").isPresent());
    }
}
