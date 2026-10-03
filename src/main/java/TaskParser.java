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

    private final List<String> errors = new ArrayList<>();

    /**
     * Читає автозапчастини з CSV-файлу.
     *
     * @param inputPath шлях до вхідного файлу
     * @return список коректних записів
     * @throws IOException якщо файл неможливо прочитати
     */
    public List<SparePart> readTasks(Path inputPath) throws IOException {

        List<String> lines = Files.readAllLines(
                inputPath,
                StandardCharsets.UTF_8
        );

        List<SparePart> tasks = new ArrayList<>();

        errors.clear();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            int lineNumber = i + 1;

            if (line.isBlank()) {
                continue;
            }

            try {
                tasks.add(SparePart.fromCsv(line));
            } catch (IllegalArgumentException exception) {
                errors.add(
                        "Рядок " + lineNumber + ": " + exception.getMessage()
                );
            }
        }

        return tasks;
    }

    /**
     * Повертає список помилок під час перевірки вхідних даних.
     *
     * @return список помилок
     */
    public List<String> getErrors() {
        return List.copyOf(errors);
    }
}