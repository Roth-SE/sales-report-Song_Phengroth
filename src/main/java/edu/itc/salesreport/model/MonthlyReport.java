package edu.itc.salesreport.model;

import java.time.YearMonth;
import java.util.List;

public record MonthlyReport(
        YearMonth month, 
        List<BranchSummary> branches, 
        BranchSummary chain, 
        List<String> topPerformers // or generic string list expected by the 4th arg
) {
    public MonthlyReport {
        branches = List.copyOf(branches);
        topPerformers = List.copyOf(topPerformers);
    }
}