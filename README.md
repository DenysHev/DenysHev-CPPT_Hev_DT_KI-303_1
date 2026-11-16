# Лабораторна робота №5

## Склад автозапчастин, варіант 4

Проєкт продовжує попередні роботи на Java 21: поліморфна модель і Stream API
збережені, а дані тепер можна типобезпечно зберігати та відновлювати через
універсальний CSV-експортер.

### Нові компоненти

- `Repository<T>` — параметризоване сховище з `add`, `snapshot` і пошуком за
  `Predicate<? super T>`;
- `SparePartRecord` — плоский запис варіанта 4 з перевіркою полів;
- `CsvColumn` — runtime-анотація назв CSV-стовпців;
- `CsvExporter` — рефлексивний експортер/імпортер для будь-якого класу з
  анотованими полями;
- `DataStorageException` — checked exception, який зберігає `cause`.

Поля CSV впорядковуються за іменем Java-поля. Для `SparePartRecord` заголовок:

```text
артикул,виробник,назва,ціна,кількість
```

Експортер використовує UTF-8, перезаписує файл через `TRUNCATE_EXISTING`,
екранує коми, лапки та перенесення рядків і читає CSV як документ, а не як
набір фізичних рядків. `SparePartRecord.fromCsvFields` відновлює типізовані
значення, тому round-trip перевіряє рівність початкових і відновлених записів.

### Збережені Stream API-запити

`InventoryStreamQueries` містить п'ять незалежних конвеєрів:

1. `filterLowStock` — відбір деталей із залишком нижче порогу 10;
2. `mapSkus` — перетворення деталей на список артикулів;
3. `inventoryValueBySupplier` — групування вартості запасу за виробником;
4. `priceStatistics` — `DoubleSummaryStatistics` для цін;
5. `topByPrice` — top-N за ціною спаданням і назвою зростанням.

`findBySku` виконує безпечний пошук через `Optional`. Для дубльованого
артикулу повертається перший запис у початковому порядку. Від'ємне N
відхиляється, нуль повертає порожній список, а завелике N повертає всі записи.

### Збірка, тести та запуск

```bash
./mvnw -B clean verify
./mvnw -B package
./mvnw -B test
java -jar target/cppt-labs.jar --input src/test/resources/input.csv
java -jar target/cppt-labs.jar --input src/test/resources/input.csv --output /tmp/report.txt
java -jar target/cppt-labs.jar --help
java -jar target/cppt-labs.jar --version
```

Версія задається єдиним значенням у `pom.xml`; команда `--version` читає
значення, підставлене Maven під час збирання JAR.

`verify` запускає JUnit і SpotBugs. CI перевіряє Maven-проєкт на Ubuntu,
Windows і macOS. Контрольний CSV для тестів зберігається в
`src/test/resources/input.csv`; кореневі вхідні та вихідні тестові файли
до репозиторію не додаються.

Нові тести перевіряють типобезпечне сховище, незмінний знімок, runtime-
анотацію, детермінований заголовок, CSV escaping, порожній набір, пошкоджені
числа, файлові помилки та збереження першопричини.
