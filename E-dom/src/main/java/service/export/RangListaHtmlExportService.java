package service.export;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dto.RangListaDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class RangListaHtmlExportService implements ExportService<RangListaDTO> {

    @Override
    public void exportData(List<RangListaDTO> data, File file) {
        try {
            // 1️⃣ HTML header i stil
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>")
                    .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">")
                    .append("<head>")
                    .append("<meta charset=\"UTF-8\"/>")
                    .append("<style>")
                    .append("body, table, td, th { font-family: 'DejaVuSans', sans-serif; }")
                    .append("body { margin: 20px; font-size: 12px; }")
                    .append("h1 { text-align: center; color: #333; }")
                    .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                    .append("th, td { border: 1px solid #000; padding: 8px; text-align: left; }")
                    .append("th { background-color: #f2f2f2; font-weight: bold; }")
                    .append("tr:nth-child(even) { background-color: #f9f9f9; }")
                    .append("</style>")
                    .append("</head>")
                    .append("<body>")
                    .append("<h1>Rang lista studenata</h1>")
                    .append("<table>");

            // 2️⃣ Dinamički header iz prve DTO mape
            if (!data.isEmpty()) {
                html.append("<thead><tr>");
                for (String kolona : data.get(0).getKolone().keySet()) {
                    html.append("<th>").append(kolona).append("</th>");
                }
                html.append("</tr></thead>");
            }

            // 3️⃣ Redovi
            html.append("<tbody>");
            for (RangListaDTO dto : data) {
                html.append("<tr>");
                for (Map.Entry<String, String> entry : dto.getKolone().entrySet()) {
                    html.append("<td>").append(escapeHtml(entry.getValue())).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody></table></body></html>");

            // 4️⃣ Generisanje PDF-a
            try (OutputStream os = new FileOutputStream(file)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();

                try (InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                    if (fontStream == null) {
                        throw new RuntimeException("Font nije nađen na /fonts/DejaVuSans.ttf");
                    }
                    builder.useFont(() -> fontStream, "DejaVuSans");
                    builder.withHtmlContent(html.toString(), "");
                    builder.toStream(os);
                    builder.run();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Greška prilikom generisanja PDF-a: " + e.getMessage(), e);
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
