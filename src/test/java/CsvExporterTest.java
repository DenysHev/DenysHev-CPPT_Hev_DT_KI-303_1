import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class CsvExporterTest {

    private final CsvExporter exporter = new CsvExporter();

    @Test
    void exportsSortedAnnotatedColumnsAndEscapesSpecialText() throws Exception {
        SparePartRecord record = new SparePartRecord(
                "A-1", "Фільтр, \"преміум\"\nновий", 4, 125.50, "Bosch");

        String csv = exporter.export(SparePartRecord.class, List.of(record));

        assertTrue(csv.startsWith("артикул,виробник,назва,ціна,кількість\n"));
        assertTrue(csv.contains("\"Фільтр, \"\"преміум\"\"\nновий\""));
    }

    @Test
    void roundTripPreservesValuesAndTruncatesExistingFile() throws Exception {
        Path file = Files.createTempFile("parts-", ".csv");
        try {
            List<SparePartRecord> expected = List.of(
                    new SparePartRecord("A-1", "Фільтр", 4, 125.50, "Bosch"),
                    new SparePartRecord("A-2", "Свічка", 10, 25.00, "NGK"));

            exporter.write(file, SparePartRecord.class, expected);
            List<SparePartRecord> actual = exporter.read(
                    file, SparePartRecord.class, SparePartRecord::fromCsvFields);

            assertEquals(expected, actual);
            assertFalse(Files.readString(file).contains("преміум"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void reportsInvalidCsvAndPreservesCause() throws Exception {
        Path file = Files.createTempFile("parts-invalid-", ".csv");
        try {
            Files.writeString(file,
                    "артикул,виробник,назва,ціна,кількість\n"
                            + "A-1,Bosch,Фільтр,not-a-number,4\n");

            DataStorageException exception = assertThrows(
                    DataStorageException.class,
                    () -> exporter.read(
                            file,
                            SparePartRecord.class,
                            SparePartRecord::fromCsvFields));

            assertTrue(exception.getMessage().contains("рядку 2"));
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
            assertTrue(exception.getCause().getCause()
                    instanceof NumberFormatException);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void supportsEmptyDatasetAndRejectsMalformedStructure() throws Exception {
        Path file = Files.createTempFile("parts-empty-", ".csv");
        try {
            exporter.write(file, SparePartRecord.class, List.of());
            assertEquals(List.of(), exporter.read(
                    file, SparePartRecord.class, SparePartRecord::fromCsvFields));

            Files.writeString(
                    file,
                    "артикул,виробник,назва,ціна,кількість\n"
                            + "A-1,Bosch,Фільтр,125.50\n");
            assertThrows(DataStorageException.class, () -> exporter.read(
                    file, SparePartRecord.class, SparePartRecord::fromCsvFields));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void reportsMissingFileAsStorageException() {
        Path missing = Path.of("target", "missing-parts.csv");

        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> exporter.read(
                        missing,
                        SparePartRecord.class,
                        SparePartRecord::fromCsvFields));

        assertTrue(exception.getMessage().contains("прочитати CSV"));
        assertTrue(exception.getCause() instanceof java.io.IOException);
    }

    @Test
    void rejectsTextAfterClosingQuoteAndUnquotedInnerQuote() throws Exception {
        Path file = Files.createTempFile("parts-quote-", ".csv");
        try {
            Files.writeString(
                    file,
                    "артикул,виробник,назва,ціна,кількість\n"
                            + "A-1,Bosch,\"Фільтр\"text,125.50,4\n");
            assertThrows(DataStorageException.class, () -> exporter.read(
                    file, SparePartRecord.class, SparePartRecord::fromCsvFields));

            Files.writeString(
                    file,
                    "артикул,виробник,назва,ціна,кількість\n"
                            + "A-1,Bosch,Фі\"льтр,125.50,4\n");
            assertThrows(DataStorageException.class, () -> exporter.read(
                    file, SparePartRecord.class, SparePartRecord::fromCsvFields));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
