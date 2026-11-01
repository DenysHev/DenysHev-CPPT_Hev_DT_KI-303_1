import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.DoubleSummaryStatistics;

/**
 * Формує текстовий звіт про склад автозапчастин.
 */
public class ReportFormatter {

    private static final String NL = System.lineSeparator();

    /**
     * Створює форматер звітів про склад.
     */
    public ReportFormatter() {
    }

    /**
     * Формує звіт.
     *
     * @param tasksCount кількість коректних записів
     * @param totalStock загальна кількість деталей
     * @param totalInventoryValue загальна вартість запасів
     * @param mostExpensivePart найдорожча деталь
     * @param errors список помилок вхідних даних
     * @return текст звіту
     */
    public String format(
            int tasksCount,
            int totalStock,
            double totalInventoryValue,
            SparePart mostExpensivePart,
            List<String> errors) {
        return format(
                tasksCount, totalStock, totalInventoryValue,
                mostExpensivePart, 0.0, errors);
    }

    /**
     * Формує звіт із поліморфно обчисленою гарантійною вартістю.
     *
     * @param tasksCount кількість коректних записів
     * @param totalStock загальна кількість
     * @param totalInventoryValue вартість запасу
     * @param mostExpensivePart найдорожча деталь
     * @param warrantyCost гарантійна вартість
     * @param errors помилки вхідних даних
     * @return текст звіту
     */
    public String format(
            int tasksCount,
            int totalStock,
            double totalInventoryValue,
            SparePart mostExpensivePart,
            double warrantyCost,
            List<String> errors) {

        String expensivePart;

        if (mostExpensivePart == null) {
            expensivePart = "Немає даних";
        } else {
            expensivePart = String.format(
                    Locale.ROOT,
                    "%s (%.2f)",
                    mostExpensivePart.name(),
                    mostExpensivePart.unitPrice()
            );
        }

        StringBuilder report = new StringBuilder();

        report.append(String.format(
                Locale.ROOT,
                "ЗВІТ: СКЛАД АВТОЗАПЧАСТИН%n"
                        + "----------------------------------------%n"
                        + "Коректних записів: %d%n"
                        + "Загальна кількість: %d%n"
                        + "Вартість запасу: %.2f%n"
                        + "Гарантійна вартість: %.2f%n"
                        + "Найдорожча деталь: %s%n",
                tasksCount,
                totalStock,
                totalInventoryValue,
                warrantyCost,
                expensivePart
        ));

        report.append(NL);
        report.append("----------------------------------------").append(NL);
        report.append("ПЕРЕВІРКА ВХІДНИХ ДАНИХ").append(NL);
        report.append("----------------------------------------").append(NL);

        if (errors.isEmpty()) {
            report.append("Помилок не виявлено.").append(NL);
        } else {
            report.append("Виявлено помилок: ")
                    .append(errors.size())
                    .append(NL);

            for (String error : errors) {
                report.append("- ").append(error).append(NL);
            }
        }

        return report.toString();
    }

    /**
     * Формує секцію звіту для потокових запитів ЛР4.
     *
     * @param threshold поріг залишку
     * @param lowStock деталі з залишком нижче порогу
     * @param skus артикули всіх деталей
     * @param valueBySupplier вартість запасу за виробником
     * @param priceStatistics статистика цін
     * @param topParts найдорожчі деталі
     * @param found знайдена деталь
     * @param missing результат пошуку відсутнього артикулу
     * @return текст секції
     */
    public String formatStreamQueries(
            int threshold,
            List<SparePart> lowStock,
            List<String> skus,
            Map<String, Double> valueBySupplier,
            DoubleSummaryStatistics priceStatistics,
            List<SparePart> topParts,
            Optional<SparePart> found,
            Optional<SparePart> missing) {
        String lowStockSkus = lowStock.stream()
                .map(SparePart::sku)
                .collect(java.util.stream.Collectors.joining(", "));
        String top = topParts.stream()
                .map(part -> String.format(
                        Locale.ROOT, "%s (%.2f)", part.name(), part.unitPrice()))
                .collect(java.util.stream.Collectors.joining(", "));
        double minimum = priceStatistics.getCount() == 0
                ? 0.0 : priceStatistics.getMin();
        double maximum = priceStatistics.getCount() == 0
                ? 0.0 : priceStatistics.getMax();
        double average = priceStatistics.getCount() == 0
                ? 0.0 : priceStatistics.getAverage();

        return String.format(
                Locale.ROOT,
                "%nПОТОКОВІ ЗАПИТИ ЛР4%n"
                        + "----------------------------------------%n"
                        + "Залишок нижче %d: [%s]%n"
                        + "Артикули: %s%n"
                        + "Вартість запасу за виробником: %s%n"
                        + "Статистика цін: кількість=%d, сума=%.2f, "
                        + "середнє=%.2f, мінімум=%.2f, максимум=%.2f%n"
                        + "Top-%d за ціною: [%s]%n"
                        + "Пошук SKU-001: %s%n"
                        + "Пошук ABSENT: %s%n",
                threshold,
                lowStockSkus,
                skus,
                valueBySupplier,
                priceStatistics.getCount(),
                priceStatistics.getSum(),
                average,
                minimum,
                maximum,
                topParts.size(),
                top,
                found.map(SparePart::name).orElse("не знайдено"),
                missing.isPresent() ? "знайдено" : "не знайдено");
    }
}