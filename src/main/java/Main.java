import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Головний клас програми.
 *
 * <p>Консольна програма для обробки даних складу автозапчастин.</p>
 */
public final class Main {

    private static final String VERSION = "1.0.0";

    private Main() {
    }

    /**
     * Точка входу в програму.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        Path input = Path.of("data", "input.csv");
        Path output = Path.of("out", "report.txt");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }

                case "--version" -> {
                    System.out.println(VERSION);
                    return;
                }

                case "--input" -> {
                    if (i + 1 >= args.length) {
                        System.out.println(
                                "Помилка: після --input потрібно вказати шлях."
                        );
                        return;
                    }
                    input = Path.of(args[++i]);
                }

                case "--output" -> {
                    if (i + 1 >= args.length) {
                        System.out.println(
                                "Помилка: після --output потрібно вказати шлях."
                        );
                        return;
                    }
                    output = Path.of(args[++i]);
                }

                default -> {
                    System.out.println("Невідомий аргумент: " + args[i]);
                    System.out.println("Використайте --help.");
                    return;
                }
            }
        }

        try {
            TaskParser parser = new TaskParser();
            TaskMetrics metrics = new TaskMetrics();
            ReportFormatter formatter = new ReportFormatter();

            List<AutoPart> tasks = parser.readTasks(input);
            InventoryMetrics inventoryMetrics = metrics.calculate(tasks);

            String report = formatter.format(
                    inventoryMetrics.validRecords(),
                    inventoryMetrics.totalStock(),
                    inventoryMetrics.totalInventoryValue(),
                    inventoryMetrics.mostExpensivePart(),
                    parser.getErrors()
            );

            System.out.println(report);

            Path outputParent = output.getParent();

            if (outputParent != null) {
                Files.createDirectories(outputParent);
            }

            Files.writeString(
                    output,
                    report,
                    StandardCharsets.UTF_8
            );

            System.out.println("Звіт записано у: " + output);

        } catch (IOException exception) {
            System.out.println(
                    "Помилка роботи з файлом: " + exception.getMessage()
            );
        }
    }

    /**
     * Виводить довідку щодо використання програми.
     */
    private static void printHelp() {
        System.out.println("""
                Використання:
                java -jar cppt-labs.jar [--help] [--version]
                    [--input <файл>] [--output <файл>]

                --help              показати довідку
                --version           показати версію програми
                --input <файл>      шлях до вхідного CSV-файлу
                --output <файл>     шлях до файлу звіту
                """);
    }
}