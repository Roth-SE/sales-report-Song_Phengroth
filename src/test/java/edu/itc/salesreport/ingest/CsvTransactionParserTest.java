package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvTransactionParserTest {

    private final TransactionParser parser = new CsvTransactionParser();

    @Test
    void parsesAValidRow() throws InvalidRowException {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,"
                + "SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR");
        
        assertEquals("PNH", t.branch());
        assertEquals(LocalDate.parse("2026-09-01"), t.date());
        assertEquals("Jasmine Rice 5kg", t.productName());
        assertEquals(2, t.quantity());
        assertEquals(new BigDecimal("6.50"), t.unitPrice());
        assertEquals(PaymentMethod.KHQR, t.paymentMethod());
        assertEquals(new BigDecimal("13.00"), t.revenue());
    }

    @Test
    void parsesAValidRowWithDiscount() throws InvalidRowException {
        var t = parser.parse("PNH,2026-09-01,PNH-000123,"
                + "SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,1.50,CASH");
        
        // (2 x 6.50) - 1.50 = 11.50
        assertEquals(new BigDecimal("11.50"), t.revenue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",                                                            // empty line
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00",         // 9 fields
        "NYC,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",    // unknown branch
        "PNH,2026-09-31,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,CASH",    // impossible date
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Weapons,2,6.50,0.00,CASH",    // unknown category
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,two,6.50,0.00,CASH",  // non-numeric quantity
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,0,6.50,0.00,CASH",    // zero quantity
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,-1.00,CASH",   // negative discount
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,15.00,CASH",   // discount > line total (15.00 > 13.00)
        "PNH,2026-09-01,PNH-1,SKU-1,Rice,Grocery,2,6.50,0.00,BITCOIN"  // unknown payment method
    })
    void rejectsInvalidRows(String line) {
        assertThrows(InvalidRowException.class, () -> parser.parse(line));
    }
}