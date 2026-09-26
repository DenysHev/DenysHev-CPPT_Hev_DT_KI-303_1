/**
 * Представляє одну автозапчастину на складі.
 *
 * @param sku артикул
 * @param name назва деталі
 * @param stock кількість на складі
 * @param unitPrice ціна однієї деталі
 * @param supplier постачальник
 */
public record Task(
        String sku,
        String name,
        int stock,
        double unitPrice,
        String supplier
) {
}
