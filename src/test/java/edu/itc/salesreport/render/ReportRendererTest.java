package edu.itc.salesreport.render;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class ReportRendererTest {

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void rendersCoreDataForAllFormats(String ext) {
        ReportRenderer renderer = ReportRenderer.forExtension(ext);
        String output = new String(renderer.render(TestReports.september()), StandardCharsets.UTF_8);

        assertTrue(output.contains("PNH"), "Must contain PNH branch");
        assertTrue(output.contains("REP"), "Must contain REP branch");
        assertTrue(output.contains("BTB"), "Must contain BTB branch");
        assertTrue(output.contains("2300.50"), "Must contain chain total revenue");
    }

    @Test
    void csvProducesExactLineForRep() {
        ReportRenderer renderer = ReportRenderer.forExtension("csv");
        String output = new String(renderer.render(TestReports.september()), StandardCharsets.UTF_8);
        assertTrue(output.contains("2026-09,REP,800.50,0.00,50,16.01"));
    }

    @Test
    void rejectsUnknownExtension() {
        assertThrows(IllegalArgumentException.class, () -> ReportRenderer.forExtension("pdf"));
    }

    @Test
    void exhaustiveSwitchCoverage() {
        ReportRenderer renderer = ReportRenderer.forExtension("html");
        // Proving exhaustive coverage without default clause
        String ext = switch (renderer) {
            case TextReportRenderer t -> t.extension();
            case HtmlReportRenderer h -> h.extension();
            case CsvReportRenderer c -> c.extension();
        };
        assertEquals("html", ext);
    }
}