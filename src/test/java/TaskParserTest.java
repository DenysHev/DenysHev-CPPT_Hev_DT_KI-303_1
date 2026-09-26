import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TaskParserTest {

    @Test
    void shouldReadValidTasks() throws Exception {
        Path file = Files.createTempFile("parts", ".csv");

        Files.writeString(file, """
                                SKU001;\u041a\u043e\u043b\u043e\u0434\u043a\u0438;10;850.50;Bosch
                                SKU002;\u0424\u0456\u043b\u044c\u0442\u0440;20;320.00;Mann
                                """);

        TaskParser parser = new TaskParser();

        List<Task> tasks = parser.readTasks(file);

        assertEquals(2, tasks.size());
        assertEquals("Колодки", tasks.get(0).name());
        assertEquals(10, tasks.get(0).stock());

        Files.deleteIfExists(file);
    }

    @Test
    void shouldIgnoreInvalidTasks() throws Exception {
        Path file = Files.createTempFile("parts", ".csv");

        Files.writeString(file, """
                                SKU001;\u041a\u043e\u043b\u043e\u0434\u043a\u0438;10;850.50;Bosch
                                BAD001;\u041d\u0435\u0441\u043f\u0440\u0430\u0432\u043d\u0430 \u0434\u0435\u0442\u0430\u043b\u044c;-5;500.00;Test
                                """);

        TaskParser parser = new TaskParser();

        List<Task> tasks = parser.readTasks(file);

        assertEquals(1, tasks.size());

        Files.deleteIfExists(file);
    }
}
