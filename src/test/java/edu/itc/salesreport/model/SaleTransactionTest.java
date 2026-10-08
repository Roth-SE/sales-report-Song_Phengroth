package edu.itc.salesreport.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleTransactionTest {

    @Test
    void testRevenueRoundingHalfUp() {
        var t = new SaleTransaction("PNH", LocalDate.now(), "R1", "S1", "P1",
                "Grocery", 3, new BigDecimal("0.335"), BigDecimal.ZERO, PaymentMethod.CASH);
        
        // 3 * 0.335 = 1.005 -> scale 2 HALF_UP -> 1.01
        assertEquals(new BigDecimal("1.01"), t.revenue());
    }

    @Test
    void branchSummaryTopProductsIsUnmodifiable() {
        List<ProductTotal> mutableList = new ArrayList<>();
        mutableList.add(new ProductTotal("S1", "P1", 10, new BigDecimal("100.00")));

        var summary = new BranchSummary("PNH", new BigDecimal("100.00"), 5, mutableList);

        // Prove the internal collection rejects mutations
        assertThrows(UnsupportedOperationException.class, () -> {
            summary.topProducts().add(new ProductTotal("S2", "P2", 1, new BigDecimal("10.00")));
        });

        // Prove changes to the original list don't leak into the record
        mutableList.add(new ProductTotal("S3", "P3", 2, new BigDecimal("20.00")));
        assertEquals(1, summary.topProducts().size());
    }

    @Test
    void branchSummaryAverageBasketHandlesZeroReceipts() {
        var summary = new BranchSummary("PNH", BigDecimal.ZERO, 0, List.of());
        assertEquals(new BigDecimal("0.00"), summary.averageBasket());
    }
}