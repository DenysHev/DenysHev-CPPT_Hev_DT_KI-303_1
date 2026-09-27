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

    private final List<String> errors = new ArrayList<>();

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

        errors.clear();

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);
            int lineNumber = i + 1;

            if (line.isBlank()) {
                continue;
            }

            String[] fields = line.split(";", -1);

            if (fields.length != EXPECTED_FIELDS) {
                errors.add(
                        "Рядок " + lineNumber
                                + ": очікується " + EXPECTED_FIELDS
                                + " полів, отримано " + fields.length
                );
                continue;
            }

            if (fields[0].isBlank()
                    || fields[1].isBlank()
                    || fields[4].isBlank()) {

                errors.add(
                        "Рядок " + lineNumber
                                + ": обов'язкове поле не може бути порожнім"
                );
                continue;
            }

            try {
                int stock = Integer.parseInt(fields[2]);
                double unitPrice = Double.parseDouble(fields[3]);

                if (stock < 0) {
                    errors.add(
                            "Рядок " + lineNumber
                                    + ": кількість товару не може бути від'ємною: "
                                    + stock
                    );
                    continue;
                }

                if (unitPrice < 0) {
                    errors.add(
                            "Рядок " + lineNumber
                                    + ": ціна не може бути від'ємною: "
                                    + unitPrice
                    );
                    continue;
                }

                tasks.add(new Task(
                        fields[0].trim(),
                        fields[1].trim(),
                        stock,
                        unitPrice,
                        fields[4].trim()
                ));

            } catch (NumberFormatException e) {
                errors.add(
                        "Рядок " + lineNumber
                                + ": кількість або ціна мають некоректний числовий формат"
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