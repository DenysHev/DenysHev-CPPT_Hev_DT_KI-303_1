import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Декларативні запити Stream API до складу автозапчастин.
 */
public final class InventoryStreamQueries {

    /**
     * Створює компонент потокових запитів.
     */
    public InventoryStreamQueries() {
    }

    /**
     * Відбирає деталі, залишок яких менший за поріг.
     *
     * @param parts деталі складу
     * @param threshold поріг залишку
     * @return новий список деталей із малим залишком
     */
    public List<SparePart> filterLowStock(
            List<SparePart> parts,
            int threshold) {
        return requireParts(parts).stream()
                .filter(part -> part.stock() < threshold)
                .toList();
    }

    /**
     * Повертає артикули деталей у початковому порядку.
     *
     * @param parts деталі складу
     * @return список артикулів
     */
    public List<String> mapSkus(List<SparePart> parts) {
        return requireParts(parts).stream()
                .map(SparePart::sku)
                .toList();
    }

    /**
     * Групує вартість запасу за виробником.
     *
     * @param parts деталі складу
     * @return вартість запасу для кожного виробника
     */
    public Map<String, Double> inventoryValueBySupplier(
            List<SparePart> parts) {
        return requireParts(parts).stream()
                .collect(Collectors.groupingBy(
                        SparePart::supplier,
                        java.util.LinkedHashMap::new,
                        Collectors.summingDouble(
                                part -> part.stock() * part.unitPrice())));
    }

    /**
     * Обчислює зведену статистику цін.
     *
     * @param parts деталі складу
     * @return кількість, сума, середнє, мінімум і максимум цін
     */
    public DoubleSummaryStatistics priceStatistics(
            List<SparePart> parts) {
        return requireParts(parts).stream()
                .collect(Collectors.summarizingDouble(SparePart::unitPrice));
    }

    /**
     * Повертає найдорожчі деталі з обмеженням кількості.
     *
     * @param parts деталі складу
     * @param limit максимальна кількість результатів
     * @return деталі за ціною спаданням і назвою зростанням
     */
    public List<SparePart> topByPrice(
            List<SparePart> parts,
            int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("N не може бути від'ємним");
        }

        return requireParts(parts).stream()
                .sorted(Comparator
                        .comparingDouble(SparePart::unitPrice)
                        .reversed()
                        .thenComparing(SparePart::name))
                .limit(limit)
                .toList();
    }

    /**
     * Шукає першу деталь за артикулом.
     *
     * @param parts деталі складу
     * @param sku артикул
     * @return знайдена деталь або {@link Optional#empty()}
     */
    public Optional<SparePart> findBySku(
            List<SparePart> parts,
            String sku) {
        Objects.requireNonNull(sku, "артикул не може бути null");
        return requireParts(parts).stream()
                .filter(part -> part.sku().equals(sku))
                .findFirst();
    }

    private List<SparePart> requireParts(List<SparePart> parts) {
        return Objects.requireNonNull(parts, "список деталей не може бути null");
    }
}
