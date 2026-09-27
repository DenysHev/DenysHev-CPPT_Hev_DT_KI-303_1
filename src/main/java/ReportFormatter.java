import java.util.List;
import java.util.Locale;

/**
 * Формує текстовий звіт про склад автозапчастин.
 */
public class ReportFormatter {

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
            Task mostExpensivePart,
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
                        + "Найдорожча деталь: %s%n",
                tasksCount,
                totalStock,
                totalInventoryValue,
                expensivePart
        ));

        report.append("\n----------------------------------------\n");
        report.append("ПЕРЕВІРКА ВХІДНИХ ДАНИХ\n");
        report.append("----------------------------------------\n");

        if (errors.isEmpty()) {
            report.append("Помилок не виявлено.\n");
        } else {
            report.append("Виявлено помилок: ")
                    .append(errors.size())
                    .append("\n");

            for (String error : errors) {
                report.append("- ")
                        .append(error)
                        .append("\n");
            }
        }

        return report.toString();
    }
}