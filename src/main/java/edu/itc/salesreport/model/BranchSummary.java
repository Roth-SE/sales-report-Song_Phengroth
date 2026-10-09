package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public record BranchSummary(
        String branch, 
        BigDecimal revenue, 
        BigDecimal discounts, 
        long receipts, 
        Map<String, BigDecimal> byCategory,
        List<ProductTotal> topProducts,
        Map<PaymentMethod, BigDecimal> byPaymentMethod
) {
    public BranchSummary {
        // Information hiding: make defensive copies of collections
        byCategory = Map.copyOf(byCategory);
        topProducts = List.copyOf(topProducts);
        byPaymentMethod = Map.copyOf(byPaymentMethod);
    }

    public BigDecimal averageBasket() {
        if (receipts == 0) {
            return new BigDecimal("0.00");
        }
        return revenue.divide(BigDecimal.valueOf(receipts), 2, RoundingMode.HALF_UP);
    }
}