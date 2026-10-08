package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record BranchSummary(String branch, BigDecimal totalRevenue, int totalReceipts, List<ProductTotal> topProducts) {
    public BranchSummary {
        topProducts = List.copyOf(topProducts); // Information hiding
    }

    public BigDecimal averageBasket() {
        if (totalReceipts == 0) {
            return new BigDecimal("0.00");
        }
        return totalRevenue.divide(BigDecimal.valueOf(totalReceipts), 2, RoundingMode.HALF_UP);
    }
}