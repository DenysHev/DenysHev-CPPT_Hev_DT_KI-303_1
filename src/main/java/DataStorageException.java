/**
 * Помилка збереження, читання або перетворення даних складу.
 */
public class DataStorageException extends Exception {

    /**
     * Створює помилку з повідомленням.
     *
     * @param message пояснення помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює помилку зі збереженням першопричини.
     *
     * @param message пояснення помилки
     * @param cause першопричина
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
