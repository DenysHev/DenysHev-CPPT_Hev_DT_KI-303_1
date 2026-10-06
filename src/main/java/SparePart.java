import java.util.Locale;

/**
 * Незмінна сутність автозапчастини на складі.
 */
public final class SparePart {

    private static final int EXPECTED_CSV_FIELDS = 5;

    private final String sku;
    private final String name;
    private final int stock;
    private final double unitPrice;
    private final String supplier;

    /**
     * Створює автозапчастину та перевіряє її доменні властивості.
     *
     * @param sku артикул
     * @param name назва деталі
     * @param stock кількість на складі
     * @param unitPrice ціна однієї деталі
     * @param supplier постачальник
     * @throws IllegalArgumentException якщо значення не відповідають правилам домену
     */
    public SparePart(
            String sku,
            String name,
            int stock,
            double unitPrice,
            String supplier) {
        if (sku == null || sku.isBlank()
                || name == null || name.isBlank()
                || supplier == null || supplier.isBlank()) {
            throw new IllegalArgumentException(
                    "обов'язкове поле не може бути порожнім"
            );
        }
        if (stock < 0) {
            throw new IllegalArgumentException(
                    "кількість товару не може бути від'ємною: " + stock
            );
        }
        if (!Double.isFinite(unitPrice)) {
            throw new IllegalArgumentException(
                    "ціна має бути скінченним числом: " + unitPrice
            );
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException(
                    "ціна не може бути від'ємною: " + unitPrice
            );
        }

        this.sku = sku.trim();
        this.name = name.trim();
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.supplier = supplier.trim();
    }

    /**
     * Створює автозапчастину з одного CSV-рядка.
     *
     * @param csvLine рядок у форматі sku;name;stock;unitPrice;supplier
     * @return створена автозапчастина
     * @throws IllegalArgumentException якщо рядок має неправильний формат
     *         або містить недопустимі значення
     */
    public static SparePart fromCsv(String csvLine) {
        if (csvLine == null) {
            throw new IllegalArgumentException("CSV-рядок не може бути null");
        }

        String[] fields = csvLine.split(";", -1);

        if (fields.length != EXPECTED_CSV_FIELDS) {
            throw new IllegalArgumentException(
                    "очікується " + EXPECTED_CSV_FIELDS
                            + " полів, отримано " + fields.length
            );
        }

        final int stock;
        final double unitPrice;

        try {
            stock = Integer.parseInt(fields[2].trim());
            unitPrice = Double.parseDouble(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "кількість або ціна мають некоректний числовий формат",
                    exception
            );
        }

        return new SparePart(
                fields[0],
                fields[1],
                stock,
                unitPrice,
                fields[4]
        );
    }

    /**
     * Повертає текстове представлення автозапчастини.
     *
     * @return форматований опис автозапчастини
     */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "%s;%s;%d;%.2f;%s",
                sku,
                name,
                stock,
                unitPrice,
                supplier
        );
    }

    /**
     * Повертає артикул автозапчастини.
     *
     * @return артикул
     */
    public String sku() {
        return sku;
    }

    /**
     * Повертає назву автозапчастини.
     *
     * @return назва деталі
     */
    public String name() {
        return name;
    }

    /**
     * Повертає кількість автозапчастин на складі.
     *
     * @return кількість деталей
     */
    public int stock() {
        return stock;
    }

    /**
     * Повертає ціну однієї автозапчастини.
     *
     * @return ціна деталі
     */
    public double unitPrice() {
        return unitPrice;
    }

    /**
     * Повертає назву постачальника автозапчастини.
     *
     * @return постачальник
     */
    public String supplier() {
        return supplier;
    }
}
