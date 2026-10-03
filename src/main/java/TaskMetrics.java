import java.util.List;

/**
 * Виконує розрахунки для складу автозапчастин.
 */
public class TaskMetrics {

    /**
     * Обчислює показники складу.
     *
     * @param tasks список автозапчастин
     * @return набір показників
     */
    public StockValue calculate(List<SparePart> tasks) {
        int totalStock = tasks.stream()
                .mapToInt(SparePart::stock)
                .sum();
        double totalInventoryValue = tasks.stream()
                .mapToDouble(task -> task.stock() * task.unitPrice())
                .sum();
        SparePart mostExpensivePart = tasks.stream()
                .max(java.util.Comparator.comparingDouble(SparePart::unitPrice))
                .orElse(null);

        return new StockValue(
                tasks.size(),
                totalStock,
                totalInventoryValue,
                mostExpensivePart
        );
    }
}
