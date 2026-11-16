import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище записів.
 *
 * @param <T> тип запису
 */
public final class Repository<T> {

    private final List<T> items = new ArrayList<>();

    /**
     * Додає запис до сховища.
     *
     * @param item запис
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "запис не може бути null"));
    }

    /**
     * Додає всі записи до сховища.
     *
     * @param newItems записи
     */
    public void addAll(Iterable<? extends T> newItems) {
        Objects.requireNonNull(newItems, "записи не можуть бути null");
        for (T item : newItems) {
            add(item);
        }
    }

    /**
     * Повертає незмінний знімок поточного стану.
     *
     * @return копія записів
     */
    public List<T> snapshot() {
        return List.copyOf(items);
    }

    /**
     * Знаходить перший запис, що відповідає умові.
     *
     * @param predicate умова пошуку
     * @return знайдений запис або порожній результат
     */
    public Optional<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "предикат не може бути null");
        return items.stream().filter(predicate).findFirst();
    }
}
