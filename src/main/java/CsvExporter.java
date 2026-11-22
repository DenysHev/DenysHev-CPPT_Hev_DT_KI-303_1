import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Універсальний CSV-експортер для плоских класів з {@link CsvColumn}.
 */
public final class CsvExporter {

    /**
     * Формує CSV-документ із заголовком і записами.
     *
     * @param type клас записів
     * @param records записи
     * @param <T> тип запису
     * @return CSV-текст із роздільником рядків LF
     * @throws DataStorageException якщо рефлексія не вдалася
     */
    public <T> String export(Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(type, "клас запису не може бути null");
        Objects.requireNonNull(records, "список записів не може бути null");
        List<Field> fields = fieldsOf(type);
        String header = fields.stream()
                .map(this::columnName)
                .map(this::escape)
                .collect(Collectors.joining(","));
        List<String> rows = new ArrayList<>();
        for (T record : records) {
            rows.add(row(fields, record));
        }
        return rows.isEmpty()
                ? header + "\n"
                : header + "\n" + String.join("\n", rows) + "\n";
    }

    /**
     * Записує CSV з явним UTF-8 і перезаписом попереднього файла.
     *
     * @param path шлях до файла
     * @param type клас запису
     * @param records записи
     * @param <T> тип запису
     * @throws DataStorageException якщо запис або рефлексія не вдалися
     */
    public <T> void write(
            Path path,
            Class<T> type,
            List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(path, "шлях не може бути null");
        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            writer.write(export(type, records));
        } catch (IOException exception) {
            throw new DataStorageException(
                    "Не вдалося записати CSV: " + path, exception);
        }
    }

    /**
     * Читає CSV, перевіряє заголовок і створює записи предметним парсером.
     *
     * @param path шлях до файла
     * @param type клас запису
     * @param parser перетворювач полів рядка на запис
     * @param <T> тип запису
     * @return відновлені записи
     * @throws DataStorageException якщо файл або CSV некоректні
     */
    public <T> List<T> read(
            Path path,
            Class<T> type,
            Function<List<String>, T> parser) throws DataStorageException {
        Objects.requireNonNull(path, "шлях не може бути null");
        Objects.requireNonNull(parser, "парсер не може бути null");
        List<List<String>> rows = parse(readText(path));
        List<Field> fields = fieldsOf(type);
        List<String> expectedHeader = fields.stream()
                .map(this::columnName)
                .toList();
        if (rows.isEmpty()) {
            throw new DataStorageException("CSV не містить заголовка");
        }
        if (!rows.get(0).equals(expectedHeader)) {
            throw new DataStorageException("CSV містить неправильний заголовок");
        }
        List<T> result = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            if (rows.get(i).size() != expectedHeader.size()) {
                throw new DataStorageException(
                        "Неправильна кількість полів у CSV-рядку "
                                + (i + 1)
                                + ": очікувалося "
                                + expectedHeader.size()
                                + ", отримано "
                                + rows.get(i).size());
            }
            try {
                result.add(Objects.requireNonNull(parser.apply(rows.get(i))));
            } catch (RuntimeException exception) {
                throw new DataStorageException(
                        "Помилка у CSV-рядку " + (i + 1), exception);
            }
        }
        return List.copyOf(result);
    }

    private String readText(Path path) throws DataStorageException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException(
                    "Не вдалося прочитати CSV: " + path, exception);
        }
    }

    private List<Field> fieldsOf(Class<?> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type;
             current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)
                        && !Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        fields.sort(Comparator.comparing(Field::getName));
        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас не має анотованих CSV-полів: " + type.getName());
        }
        return fields;
    }

    private String columnName(Field field) {
        return field.getAnnotation(CsvColumn.class).value();
    }

    private <T> String row(List<Field> fields, T record)
            throws DataStorageException {
        Objects.requireNonNull(record, "запис не може бути null");
        try {
            return fields.stream()
                    .map(field -> value(field, record))
                    .map(this::escape)
                    .collect(Collectors.joining(","));
        } catch (RuntimeException exception) {
            throw new DataStorageException(
                    "Не вдалося прочитати значення CSV-поля", exception);
        }
    }

    private String value(Field field, Object record) {
        try {
            if (!field.trySetAccessible()) {
                throw new IllegalStateException(
                        "Немає доступу до поля " + field.getName());
            }
            Object value = field.get(record);
            return value == null ? "" : value.toString();
        } catch (ReflectiveOperationException | SecurityException exception) {
            throw new IllegalStateException(
                    "Немає доступу до поля " + field.getName(), exception);
        }
    }

    private String escape(String value) {
        if (value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<List<String>> parse(String text) throws DataStorageException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        int state = 0; // 0=start, 1=unquoted, 2=quoted, 3=after_quote
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1);
        }
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (state == 2) {
                if (current == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        state = 3;
                    }
                } else {
                    if (current == '\r') {
                        field.append('\n');
                        if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                            i++;
                        }
                    } else {
                        field.append(current);
                    }
                }
            } else if (state == 3) {
                if (current == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    state = 0;
                } else if (current == '\n' || current == '\r') {
                    row.add(field.toString());
                    rows.add(List.copyOf(row));
                    row = new ArrayList<>();
                    field.setLength(0);
                    state = 0;
                    if (current == '\r' && i + 1 < text.length()
                            && text.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    throw new DataStorageException(
                            "Некоректний символ після закриття CSV-поля");
                }
            } else if (state == 0) {
                if (current == '"') {
                    state = 2;
                } else if (current == ',') {
                    row.add("");
                } else if (current == '\n' || current == '\r') {
                    row.add("");
                    rows.add(List.copyOf(row));
                    row = new ArrayList<>();
                    if (current == '\r' && i + 1 < text.length()
                            && text.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    field.append(current);
                    state = 1;
                }
            } else if (state == 1) {
                if (current == '"') {
                    throw new DataStorageException(
                            "Лапка всередині незаповненого лапками поля");
                } else if (current == ',') {
                    row.add(field.toString());
                    field.setLength(0);
                    state = 0;
                } else if (current == '\n' || current == '\r') {
                    row.add(field.toString());
                    rows.add(List.copyOf(row));
                    row = new ArrayList<>();
                    field.setLength(0);
                    state = 0;
                    if (current == '\r' && i + 1 < text.length()
                            && text.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    field.append(current);
                }
            }
        }
        if (state == 2) {
            throw new DataStorageException("Незакриті лапки у CSV");
        }
        if (state == 3 || state == 1 || field.length() != 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(List.copyOf(row));
        }
        return rows;
    }
}
