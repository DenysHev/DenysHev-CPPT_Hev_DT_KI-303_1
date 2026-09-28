import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TaskParserTest {

    @TempDir
    Path tempDir;

    private Path writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("input.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void shouldReadValidTasks() throws IOException {
        Path file = writeCsv("""
                SKU001;Колодки;10;850.50;Bosch
                SKU002;Фільтр;20;320.00;Mann
                """);

        List<Task> tasks = new TaskParser().readTasks(file);

        assertEquals(2, tasks.size());
        assertEquals("Колодки", tasks.get(0).name());
        assertEquals("Фільтр", tasks.get(1).name());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // рядок                                          | valid | reason
        "SKU001;Колодки;10;850.50;Bosch                   | 1     | ok",
        "SKU002;Фільтр;20;320.00;Mann                     | 1     | ok",
        "SKU003;Амортизатор;30;-1250.00;KYB               | 0     | negative price",
        "SKU004;Свічка;200;abc;NGK                        | 0     | non-numeric price",
        "SKU005;Колодка;-5;100.00;Test                    | 0     | negative stock",
        "SKU006;Колодка;10;100.00;                        | 0     | empty supplier",   // порожній sku? див. нижче
        "SKU007;Мало полів;10;100.00                      | 0     | 4 fields",
        "SKU008;Багато;1;2;3;Test                         | 0     | 6 fields",
        "SKU009;Ціна NaN;10;NaN;Test                      | 0     | NaN",
        "SKU010;Ціна Infinity;10;Infinity;Test            | 0     | +Inf",
        "SKU011;Ціна -Infinity;10;-Infinity;Test          | 0     | -Inf"
    })
    void parsesOrRejects(String line, int expectedValid, String reason)
            throws IOException {

        Path file = writeCsv(line + "\n");
        TaskParser parser = new TaskParser();

        List<Task> tasks = parser.readTasks(file);

        assertEquals(expectedValid, tasks.size(),
        "Невірна кількість валідних записів для: " + line);

        if (expectedValid == 0) {
            assertEquals(1, parser.getErrors().size(),
                    "має бути рівно 1 помилка для: " + line);
        }
    }

    @Test
    void emptyAndBlankLinesAreIgnored() throws IOException {
        Path file = writeCsv("\n   \nSKU001;Колодки;10;100.00;Bosch\n\n");

        List<Task> tasks = new TaskParser().readTasks(file);

        assertEquals(1, tasks.size());
    }

    @Test
    void allInvalidRecordsProduceEmptyListAndNoException() throws IOException {
        Path file = writeCsv("""
                BAD;negative stock;-1;100.00;Test
                BAD;NaN price;10;NaN;Test
                BAD;few fields;10;100.00
                """);

        TaskParser parser = new TaskParser();
        List<Task> tasks = parser.readTasks(file);

        assertTrue(tasks.isEmpty());
        assertEquals(3, parser.getErrors().size());
    }

    @Test
    void emptyFieldInRequiredPositionIsRejected() throws IOException {
        // 5 полів, але sku порожній
        Path file = writeCsv(";Колодки;10;100.00;Bosch\n");

        TaskParser parser = new TaskParser();
        List<Task> tasks = parser.readTasks(file);

        assertTrue(tasks.isEmpty());
        assertEquals(1, parser.getErrors().size());
    }
}