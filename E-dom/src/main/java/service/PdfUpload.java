package service;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

public class PdfUpload {

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
            return null;
        }
    }
}
