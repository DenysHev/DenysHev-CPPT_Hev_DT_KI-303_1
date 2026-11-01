# Лабораторна робота №4

## Склад автозапчастин, варіант 4

Проєкт демонструє потокову обробку даних складу автозапчастин на Java 21.
Модель і CSV-контракт попередніх робіт збережені:

```text
sku;name;stock;unitPrice;supplier
```

### Stream API-запити

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

`verify` запускає JUnit і SpotBugs. CI перевіряє Maven-проєкт на Ubuntu,
Windows і macOS. Контрольний CSV для тестів зберігається в
`src/test/resources/input.csv`; кореневі вхідні та вихідні тестові файли
до репозиторію не додаються.
