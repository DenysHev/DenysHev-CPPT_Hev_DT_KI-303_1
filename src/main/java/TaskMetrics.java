import java.util.Comparator;
import java.util.List;

/**
 * Виконує розрахунки для складу автозапчастин.
 */
public class TaskMetrics {

    /**
     * Обчислює загальну кількість деталей.
     *
     * @param tasks список автозапчастин
     * @return загальна кількість
     */
    public int totalStock(List<Task> tasks) {
        return tasks.stream()
                .mapToInt(Task::stock)
                .sum();
    }

    /**
     * Обчислює загальну вартість запасів.
     *
     * @param tasks список автозапчастин
     * @return загальна вартість
     */
    public double totalInventoryValue(List<Task> tasks) {
        return tasks.stream()
                .mapToDouble(task -> task.stock() * task.unitPrice())
                .sum();
    }

    /**
     * Знаходить найдорожчу деталь.
     *
     * @param tasks список автозапчастин
     * @return найдорожча деталь або null
     */
    public Task mostExpensivePart(List<Task> tasks) {
        return tasks.stream()
                .max(Comparator.comparingDouble(Task::unitPrice))
                .orElse(null);
    }
}
