/**
 * Сумісна замінна автозапчастина.
 */
public final class ReplacementPart extends SparePart {

    private static final double WARRANTY_RATE = 0.10;

    /**
     * Створює замінну деталь.
     *
     * @param sku артикул
     * @param name назва
     * @param stock кількість
     * @param unitPrice ціна
     * @param supplier постачальник
     */
    public ReplacementPart(
            String sku, String name, int stock, double unitPrice, String supplier) {
        super(sku, name, stock, unitPrice, supplier);
    }

    /** {@inheritDoc} */
    @Override
    public PartKind kind() {
        return PartKind.REPLACEMENT;
    }

    /** {@inheritDoc} */
    @Override
    public double warrantyCost() {
        return stock() * unitPrice() * WARRANTY_RATE;
    }
}
