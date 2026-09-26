import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас для читання та перевірки CSV-файлу з автозапчастинами.
 */
public class TaskParser {

    private static final int EXPECTED_FIELDS = 5;

    /**
     * Читає автозапчастини з CSV-файлу.
     *
     * @param inputPath шлях до вхідного файлу
     * @return список коректних записів
     * @throws IOException якщо файл неможливо прочитати
     */
    public List<Task> readTasks(Path inputPath) throws IOException {

        List<String> lines = Files.readAllLines(
                inputPath,
                StandardCharsets.UTF_8
        );

        List<Task> tasks = new ArrayList<>();

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            String[] fields = line.split(";", -1);

            if (fields.length != EXPECTED_FIELDS) {
                continue;
            }

            if (fields[0].isBlank()
                    || fields[1].isBlank()
                    || fields[4].isBlank()) {
                continue;
            }

            try {
                int stock = Integer.parseInt(fields[2]);
                double unitPrice = Double.parseDouble(fields[3]);

                if (stock < 0 || unitPrice < 0) {
                    continue;
                }

                tasks.add(new Task(
                        fields[0].trim(),
                        fields[1].trim(),
                        stock,
                        unitPrice,
                        fields[4].trim()
                ));

            } catch (NumberFormatException ignored) {
                // Некоректний запис пропускається.
            }
        }

        return tasks;
    }
}
