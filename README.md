# Лабораторна робота №3

## Склад автозапчастин, варіант 4

Проєкт демонструє поліморфну модель складу автозапчастин на Java 21.
Формат даних сумісний із лабораторною роботою №2:

```text
sku;name;stock;unitPrice;supplier
```

### Модель

`SparePart` — абстрактний базовий клас зі спільними полями та інваріантами.
Конкретні підтипи:

- `OriginalPart` — оригінальна деталь, гарантійна вартість становить 20%
  вартості її запасу;
- `ReplacementPart` — замінна деталь, гарантійна вартість становить 10%.

`PartKind` містить категорії `ORIGINAL` і `REPLACEMENT`. У CSV старі артикули
`SKU-...` вважаються оригінальними, а `REP-...` і `REPL-...` — замінними.
Завдяки цьому старий формат із п'яти полів не змінюється.

Поліморфна операція `warrantyCost()` викликається через спільний тип
`SparePart`; центрального `if` або `switch` за підтипом немає.

### Показники звіту

Зберігаються всі показники ЛР2:

1. кількість коректних записів;
2. загальна кількість деталей;
3. вартість запасу;
4. найдорожча деталь.

Додатково звіт показує сумарну гарантійну вартість.

### Збірка, тести та запуск

```bash
./mvnw -B clean verify
./mvnw -B package
./mvnw -B test
java -jar target/cppt-labs.jar --input data/input.csv
java -jar target/cppt-labs.jar --input data/input.csv --output out/report.txt
java -jar target/cppt-labs.jar --help
java -jar target/cppt-labs.jar --version
```

`verify` запускає JUnit і SpotBugs. GitHub Actions виконує перевірку на
Ubuntu, Windows і macOS та публікує JAR як артефакт.
