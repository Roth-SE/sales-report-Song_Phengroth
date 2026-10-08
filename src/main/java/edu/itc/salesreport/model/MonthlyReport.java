package edu.itc.salesreport.model;

import java.time.YearMonth;
import java.util.Map;

public record MonthlyReport(YearMonth month, Map<String, BranchSummary> branchSummaries) {
    public MonthlyReport {
        branchSummaries = Map.copyOf(branchSummaries); // Information hiding
    }
}