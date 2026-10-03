/**
 * Набір обчислених показників складу автозапчастин.
 *
 * @param validRecords кількість коректних записів
 * @param totalStock загальна кількість деталей
 * @param totalInventoryValue загальна вартість запасів
 * @param mostExpensivePart найдорожча деталь або {@code null}
 */
public record StockValue(
        int validRecords,
        int totalStock,
        double totalInventoryValue,
        SparePart mostExpensivePart
) {
}
