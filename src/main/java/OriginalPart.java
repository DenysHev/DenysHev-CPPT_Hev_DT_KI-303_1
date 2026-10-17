/**
 * Оригінальна автозапчастина виробника.
 */
public final class OriginalPart extends SparePart {

    private static final double WARRANTY_RATE = 0.20;

    /**
     * Створює оригінальну деталь.
     *
     * @param sku артикул
     * @param name назва
     * @param stock кількість
     * @param unitPrice ціна
     * @param supplier постачальник
     */
    public OriginalPart(
            String sku, String name, int stock, double unitPrice, String supplier) {
        super(sku, name, stock, unitPrice, supplier);
    }

    /** {@inheritDoc} */
    @Override
    public PartKind kind() {
        return PartKind.ORIGINAL;
    }

    /** {@inheritDoc} */
    @Override
    public double warrantyCost() {
        return stock() * unitPrice() * WARRANTY_RATE;
    }
}
