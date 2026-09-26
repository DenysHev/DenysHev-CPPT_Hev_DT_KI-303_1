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

        return """
                ЗВІТ: СКЛАД АВТОЗАПЧАСТИН
                ----------------------------------------
                Коректних записів: %d
                Загальна кількість: %d
                Вартість запасу: %.2f
                Найдорожча деталь: %s
                """.formatted(
                tasksCount,
                totalStock,
                totalInventoryValue,
                expensivePart
        );
    }
}
