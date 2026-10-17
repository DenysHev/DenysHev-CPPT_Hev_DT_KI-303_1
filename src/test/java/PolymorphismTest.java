import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class PolymorphismTest {

    @Test
    void csvFactoryCreatesBothSubtypes() {
        SparePart original = SparePart.fromCsv(
                "SKU-001;Колодка;10;100.00;Bosch");
        SparePart replacement = SparePart.fromCsv(
                "REP-002;Фільтр;10;100.00;Mann");

        assertTrue(original instanceof OriginalPart);
        assertTrue(replacement instanceof ReplacementPart);
        assertEquals(PartKind.ORIGINAL, original.kind());
        assertEquals(PartKind.REPLACEMENT, replacement.kind());
    }

    @Test
    void subtypesHaveDifferentWarrantyBehavior() {
        SparePart original = new OriginalPart(
                "SKU-001", "Колодка", 10, 100.00, "Bosch");
        SparePart replacement = new ReplacementPart(
                "REP-002", "Колодка", 10, 100.00, "Mann");

        assertEquals(200.00, original.warrantyCost(), 0.001);
        assertEquals(100.00, replacement.warrantyCost(), 0.001);
    }

    @Test
    void metricsCallsPolymorphicMethodThroughBaseType() {
        TaskMetrics metrics = new TaskMetrics();
        List<SparePart> parts = List.of(
                new OriginalPart("SKU-001", "A", 2, 100.00, "Bosch"),
                new ReplacementPart("REP-002", "B", 3, 100.00, "Mann"));

        assertEquals(70.00, metrics.calculateWarrantyCost(parts), 0.001);
    }

    @Test
    void equalPartsHaveSameHashAndWorkInHashSet() {
        SparePart first = new OriginalPart(
                "SKU-001", "Колодка", 10, 100.00, "Bosch");
        SparePart second = new OriginalPart(
                "SKU-001", "Колодка", 10, 100.00, "Bosch");
        Set<SparePart> parts = new HashSet<>(List.of(first));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(parts.contains(second));
        assertEquals(1, parts.size());
    }

    @Test
    void DifferentSubtypesAreNotEqualForSameValues() {
        SparePart original = new OriginalPart(
                "SKU-001", "Колодка", 10, 100.00, "Supplier");
        SparePart replacement = new ReplacementPart(
                "SKU-001", "Колодка", 10, 100.00, "Supplier");

        assertFalse(original.equals(replacement));
    }
}
