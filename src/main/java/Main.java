import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

/**
 * Головний клас програми.
 *
 * <p>Консольна програма для обробки даних складу автозапчастин.</p>
 */
public final class Main {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int TOP_LIMIT = 5;

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
        Path csvOutput = Path.of("out", "parts.csv");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }

                case "--version" -> {
                    System.out.println(applicationVersion());
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

                case "--csv-output" -> {
                    if (i + 1 >= args.length) {
                        System.out.println(
                                "Помилка: після --csv-output потрібно вказати шлях."
                        );
                        return;
                    }
                    csvOutput = Path.of(args[++i]);
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
            InventoryStreamQueries queries = new InventoryStreamQueries();
            Repository<SparePartRecord> repository = new Repository<>();

            List<SparePart> tasks = readParts(
                    input,
                    parser,
                    repository,
                    csvOutput);
            StockValue inventoryMetrics = metrics.calculate(tasks);

            String report = formatter.format(
                    inventoryMetrics.validRecords(),
                    inventoryMetrics.totalStock(),
                    inventoryMetrics.totalInventoryValue(),
                    inventoryMetrics.mostExpensivePart(),
                    metrics.calculateWarrantyCost(tasks),
                    parser.getErrors()
            );
            report += formatter.formatStreamQueries(
                    LOW_STOCK_THRESHOLD,
                    queries.filterLowStock(tasks, LOW_STOCK_THRESHOLD),
                    queries.mapSkus(tasks),
                    queries.inventoryValueBySupplier(tasks),
                    queries.priceStatistics(tasks),
                    queries.topByPrice(tasks, TOP_LIMIT),
                    queries.findBySku(tasks, "SKU001"),
                    queries.findBySku(tasks, "ABSENT")
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
        } catch (DataStorageException exception) {
            System.out.println(
                    "Помилка сховища CSV: " + exception.getMessage()
            );
            if (exception.getCause() != null) {
                System.out.println(
                        "Причина: " + exception.getCause().getMessage());
            }
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
                    [--csv-output <файл>]

                --help              показати довідку
                --version           показати версію програми
                --input <файл>      шлях до вхідного CSV-файлу
                --output <файл>     шлях до файлу звіту
                --csv-output <файл> шлях для збереження записів у новому CSV
                """);
    }

    private static List<SparePart> readParts(
            Path input,
            TaskParser legacyParser,
            Repository<SparePartRecord> repository,
            Path csvOutput)
            throws IOException, DataStorageException {
        String firstLine = Files.readAllLines(
                input,
                StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .findFirst()
                .orElse("");

        if (firstLine.startsWith("артикул") && firstLine.contains(",")) {
            CsvExporter exporter = new CsvExporter();
            List<SparePartRecord> records = exporter.read(
                    input,
                    SparePartRecord.class,
                    SparePartRecord::fromCsvFields);
            repository.addAll(records);

            Path csvParent = csvOutput.getParent();
            if (csvParent != null) {
                Files.createDirectories(csvParent);
            }
            exporter.write(csvOutput, SparePartRecord.class,
                    repository.snapshot());

            List<SparePart> parts = new ArrayList<>();
            for (SparePartRecord record : repository.snapshot()) {
                parts.add(new OriginalPart(
                        record.article(),
                        record.name(),
                        record.quantity(),
                        record.price(),
                        record.manufacturer()));
            }
            return parts;
        }

        List<SparePart> legacyParts = legacyParser.readTasks(input);
        Repository<SparePartRecord> legacyRepository =
                new Repository<>();
        for (SparePart part : legacyParts) {
            legacyRepository.add(new SparePartRecord(
                    part.sku(),
                    part.name(),
                    part.stock(),
                    part.unitPrice(),
                    part.supplier()));
        }

        CsvExporter exporter = new CsvExporter();
        Path csvParent = csvOutput.getParent();
        if (csvParent != null) {
            Files.createDirectories(csvParent);
        }
        exporter.write(
                csvOutput,
                SparePartRecord.class,
                legacyRepository.snapshot());
        return legacyParts;
    }

    /**
     * Reads the application version generated from the Maven project version.
     *
     * @return application version
     */
    private static String applicationVersion() {
        Properties properties = new Properties();
        try (InputStream stream = Main.class.getClassLoader()
                .getResourceAsStream("build-info.properties")) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Не знайдено build-info.properties");
            }
            properties.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не вдалося прочитати версію застосунку", exception);
        }

        String version = properties.getProperty("application.version");
        if (version == null || version.isBlank()) {
            throw new IllegalStateException(
                    "Версія застосунку відсутня у build-info.properties");
        }
        return version;
    }
}