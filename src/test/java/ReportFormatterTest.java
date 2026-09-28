import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ReportFormatterTest {

    private final ReportFormatter formatter = new ReportFormatter();

    @Test
    void reportUsesSystemLineSeparator() {
        String report = formatter.format(0, 0, 0.0, null, List.of());
        assertTrue(report.contains(System.lineSeparator()));
    }

    @Test
    void reportHasNoLiteralPercentN() {
        String report = formatter.format(0, 0, 0.0, null, List.of());
        assertFalse(report.contains("%n"),
                "у звіті не повинно бути буквальних %n");
    }

    @Test
    void reportHasNoStrayBackslashN() {
        String report = formatter.format(0, 0, 0.0, null, List.of());
        String nl = System.lineSeparator();
        String stripped = report.replace(nl, "");
        assertFalse(stripped.contains("\n"));
        assertFalse(stripped.contains("\r"));
    }

    @Test
    void reportIncludesCountsAndTotals() {
        String report = formatter.format(4, 400, 108475.00, null, List.of());
        assertTrue(report.contains("4"));
        assertTrue(report.contains("400"));
        assertTrue(report.contains("108475"));
    }

    @Test
    void noErrorsProducesNoErrorList() {
        String report = formatter.format(0, 0, 0.0, null, List.of());
        assertTrue(report.contains("Помилок не виявлено"));
    }

    @Test
    void errorsAreListedOnePerLine() {
        String report = formatter.format(
                0, 0, 0.0, null,
                List.of("Рядок 1: погано", "Рядок 2: теж погано"));

        assertTrue(report.contains("Рядок 1: погано"));
        assertTrue(report.contains("Рядок 2: теж погано"));
        assertTrue(report.contains("2"));
    }

    @Test
    void nullMostExpensivePartIsHandled() {
        String report = formatter.format(0, 0, 0.0, null, List.of());
        assertTrue(report.contains("Немає даних"));
    }
}