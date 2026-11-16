import java.util.List;

/**
 * Плоский запис автозапчастини для CSV-зберігання.
 *
 * @param article артикул
 * @param name назва
 * @param quantity кількість на складі
 * @param price ціна одиниці
 * @param manufacturer виробник
 */
public record SparePartRecord(
        @CsvColumn("артикул") String article,
        @CsvColumn("назва") String name,
        @CsvColumn("кількість") int quantity,
        @CsvColumn("ціна") double price,
        @CsvColumn("виробник") String manufacturer) {

    /**
     * Перевіряє інваріанти запису.
     */
    public SparePartRecord {
        requireText(article, "артикул");
        requireText(name, "назва");
        requireText(manufacturer, "виробник");
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "кількість не може бути від'ємною");
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException(
                    "ціна має бути невід'ємним скінченним числом");
        }
    }

    /**
     * Створює запис із рядка даних у порядку анотованих стовпців.
     *
     * @param fields поля article, manufacturer, name, price, quantity
     * @return відновлений запис
     */
    public static SparePartRecord fromCsvFields(List<String> fields) {
        if (fields.size() != 5) {
            throw new IllegalArgumentException(
                    "очікувалося 5 полів, отримано " + fields.size());
        }
        try {
            return new SparePartRecord(
                    fields.get(0),
                    fields.get(2),
                    Integer.parseInt(fields.get(4)),
                    Double.parseDouble(fields.get(3)),
                    fields.get(1));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "кількість або ціна має неправильний формат", exception);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " не може бути порожнім");
        }
    }
}
