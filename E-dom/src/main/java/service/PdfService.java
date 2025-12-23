package service;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class PdfService {

    // Metoda za upload PDF-a i vraća Base64 string
    public static String uploadPdf(Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Odaberi PDF dokument");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF fajl", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file == null) return null;

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metoda za prikaz PDF-a iz Base64 stringa
    public static void prikaziPdf(String base64, String naziv) {
        if (base64 == null || base64.isEmpty()) {
            System.out.println("PDF dokument ne postoji.");
            return;
        }

        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64);
            File tempFile = File.createTempFile(naziv.replaceAll("\\s+", "_"), ".pdf");
            Files.write(tempFile.toPath(), pdfBytes);
            tempFile.deleteOnExit();

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                System.out.println("Otvaranje PDF-a nije podržano na ovom sistemu.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
