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
    public InventoryMetrics calculate(List<AutoPart> tasks) {
        int totalStock = tasks.stream()
                .mapToInt(AutoPart::stock)
                .sum();
        double totalInventoryValue = tasks.stream()
                .mapToDouble(task -> task.stock() * task.unitPrice())
                .sum();
        AutoPart mostExpensivePart = tasks.stream()
                .max(java.util.Comparator.comparingDouble(AutoPart::unitPrice))
                .orElse(null);

        return new InventoryMetrics(
                tasks.size(),
                totalStock,
                totalInventoryValue,
                mostExpensivePart
        );
    }
}
