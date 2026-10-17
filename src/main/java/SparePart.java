import java.util.Locale;
import java.util.Objects;

/**
 * Абстрактний базовий тип автозапчастини на складі.
 *
 * <p>Клас містить спільний стан та інваріанти всіх деталей. Відмінну
 * гарантійну вартість реалізують конкретні підтипи.</p>
 */
public abstract class SparePart {

    private static final int EXPECTED_CSV_FIELDS = 5;

    private final String sku;
    private final String name;
    private final int stock;
    private final double unitPrice;
    private final String supplier;

    /**
     * Створює деталь і перевіряє спільні доменні правила.
     *
     * @param sku артикул
     * @param name назва деталі
     * @param stock кількість на складі
     * @param unitPrice ціна однієї деталі
     * @param supplier постачальник
     * @throws IllegalArgumentException якщо значення неприпустимі
     */
    protected SparePart(
            String sku,
            String name,
            int stock,
            double unitPrice,
            String supplier) {
        if (sku == null || sku.isBlank()
                || name == null || name.isBlank()
                || supplier == null || supplier.isBlank()) {
            throw new IllegalArgumentException(
                    "обов'язкове поле не може бути порожнім");
        }
        if (stock < 0) {
            throw new IllegalArgumentException(
                    "кількість товару не може бути від'ємною: " + stock);
        }
        if (!Double.isFinite(unitPrice)) {
            throw new IllegalArgumentException(
                    "ціна має бути скінченним числом: " + unitPrice);
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException(
                    "ціна не може бути від'ємною: " + unitPrice);
        }

        this.sku = sku.trim();
        this.name = name.trim();
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.supplier = supplier.trim();
    }

    /**
     * Створює конкретний підтип з CSV-рядка.
     *
     * <p>Артикул із префіксом {@code REP-} або {@code REPL-} означає
     * замінну деталь. Інші допустимі артикули, зокрема старі {@code SKU-},
     * означають оригінальну деталь.</p>
     *
     * @param csvLine рядок sku;name;stock;unitPrice;supplier
     * @return створена автозапчастина
     * @throws IllegalArgumentException якщо формат або значення неправильні
     */
    public static SparePart fromCsv(String csvLine) {
        if (csvLine == null) {
            throw new IllegalArgumentException("CSV-рядок не може бути null");
        }

        String[] fields = csvLine.split(";", -1);
        if (fields.length != EXPECTED_CSV_FIELDS) {
            throw new IllegalArgumentException(
                    "очікується " + EXPECTED_CSV_FIELDS
                            + " полів, отримано " + fields.length);
        }

        final int stock;
        final double unitPrice;
        try {
            stock = Integer.parseInt(fields[2].trim());
            unitPrice = Double.parseDouble(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "кількість або ціна мають некоректний числовий формат",
                    exception);
        }

        if (fields[0].trim().toUpperCase(Locale.ROOT).startsWith("REP-")
                || fields[0].trim().toUpperCase(Locale.ROOT).startsWith("REPL-")) {
            return new ReplacementPart(
                    fields[0], fields[1], stock, unitPrice, fields[4]);
        }
        return new OriginalPart(
                fields[0], fields[1], stock, unitPrice, fields[4]);
    }

    /**
     * Повертає категорію деталі.
     *
     * @return категорія
     */
    public abstract PartKind kind();

    /**
     * Обчислює гарантійну вартість запасу цієї деталі.
     *
     * @return гарантійна вартість
     */
    public abstract double warrantyCost();

    /**
     * Повертає артикул.
     *
     * @return артикул
     */
    public final String sku() {
        return sku;
    }

    /**
     * Повертає назву деталі.
     *
     * @return назва
     */
    public final String name() {
        return name;
    }

    /**
     * Повертає кількість на складі.
     *
     * @return кількість
     */
    public final int stock() {
        return stock;
    }

    /**
     * Повертає ціну однієї деталі.
     *
     * @return ціна
     */
    public final double unitPrice() {
        return unitPrice;
    }

    /**
     * Повертає постачальника.
     *
     * @return постачальник
     */
    public final String supplier() {
        return supplier;
    }

    /**
     * Порівнює деталі за типом і всіма спільними даними.
     *
     * @param other інший об'єкт
     * @return чи є деталі рівними
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        SparePart part = (SparePart) other;
        return stock == part.stock
                && Double.compare(unitPrice, part.unitPrice) == 0
                && sku.equals(part.sku)
                && name.equals(part.name)
                && supplier.equals(part.supplier);
    }

    /**
     * Обчислює хеш за критерієм рівності.
     *
     * @return хеш-код
     */
    @Override
    public final int hashCode() {
        return Objects.hash(
                getClass(), sku, name, stock, unitPrice, supplier);
    }

    /**
     * Повертає CSV-представлення деталі.
     *
     * @return форматований рядок
     */
    @Override
    public final String toString() {
        return String.format(
                Locale.ROOT,
                "%s;%s;%d;%.2f;%s",
                sku, name, stock, unitPrice, supplier);
    }
}
