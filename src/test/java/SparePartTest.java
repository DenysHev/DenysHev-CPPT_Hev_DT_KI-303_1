import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SparePartTest {

    @Test
    void validTaskTrimsTextValues() {
        SparePart task = new SparePart(
                " SKU001 ",
                " Колодки ",
                10,
                850.50,
                " Bosch "
        );

        assertEquals("SKU001", task.sku());
        assertEquals("Колодки", task.name());
        assertEquals(10, task.stock());
        assertEquals(850.50, task.unitPrice());
        assertEquals("Bosch", task.supplier());
    }

    @Test
    void emptyRequiredFieldIsRejectedDuringConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SparePart("", "Колодки", 10, 100.00, "Bosch")
        );
    }

    @Test
    void negativeStockIsRejectedDuringConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SparePart("SKU001", "Колодки", -1, 100.00, "Bosch")
        );
    }

    @Test
    void invalidPriceIsRejectedDuringConstruction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SparePart("SKU001", "Колодки", 10, Double.NaN, "Bosch")
        );
    }

    @Test
    void fromCsvCreatesValidPart() {
        SparePart part = SparePart.fromCsv(
                " SKU001 ; Колодки ; 10 ; 850.5 ; Bosch "
        );

        assertEquals("SKU001", part.sku());
        assertEquals("Колодки", part.name());
        assertEquals(10, part.stock());
        assertEquals(850.5, part.unitPrice());
        assertEquals("Bosch", part.supplier());
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SparePart.fromCsv("SKU001;Колодки;10;850.50")
        );
    }

    @Test
    void fromCsvRejectsInvalidNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SparePart.fromCsv("SKU001;Колодки;abc;850.50;Bosch")
        );
    }

    @Test
    void toStringUsesCsvFormatAndTwoFractionDigits() {
        SparePart part = new SparePart(
                "SKU001", "Колодки", 10, 850.5, "Bosch"
        );

        assertEquals(
                "SKU001;Колодки;10;850.50;Bosch",
                part.toString()
        );
    }
}
