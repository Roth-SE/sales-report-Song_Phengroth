package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;
import java.nio.charset.StandardCharsets;

public final class HtmlReportRenderer implements ReportRenderer {
    @Override
    public String extension() { return "html"; }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("<table>\n")
          .append("<tr><th>Branch</th><th>Revenue</th><th>Receipts</th><th>Avg basket</th></tr>\n");
        
        for (BranchSummary b : report.branches()) { line(sb, b); }
        line(sb, report.chain());
        
        sb.append("</table>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void line(StringBuilder sb, BranchSummary b) {
        sb.append("<tr>")
          .append("<td>").append(escape(b.branch())).append("</td>")
          .append("<td>").append(b.revenue()).append("</td>")
          .append("<td>").append(b.receipts()).append("</td>")
          .append("<td>").append(b.averageBasket()).append("</td>")
          .append("</tr>\n");
    }

    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}