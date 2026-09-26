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
     * @return текст звіту
     */
    public String format(
        int tasksCount,
        int totalStock,
        double totalInventoryValue,
        Task mostExpensivePart) {

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

        return String.format(
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
        );
    }
}
