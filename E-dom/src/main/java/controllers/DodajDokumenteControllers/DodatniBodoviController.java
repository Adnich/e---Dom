package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import model.Dokument;
import model.VrstaDokumenta;
import service.PdfUpload;

import java.time.LocalDate;
import java.util.List;

public class DodatniBodoviController {

    @FXML
    private ComboBox<VrstaDokumenta> cmbDokument;

    @FXML
    private Label lblPdf;

    private int prijavaId;
    private String pdfBase64;

    public void init(int prijavaId, List<VrstaDokumenta> dokumenti) {
        this.prijavaId = prijavaId;
        cmbDokument.getItems().addAll(dokumenti);
    }

    @FXML
    private void onDodajPdf() {
        pdfBase64 = PdfUpload.uploadPdf(lblPdf.getScene().getWindow());

        if (pdfBase64 != null) {
            lblPdf.setText("PDF dodat");
        } else {
            lblPdf.setText("PDF nije dodat");
        }
    }

    @FXML
    private void dodajDokument() {
        VrstaDokumenta vrsta = cmbDokument.getValue();
        if (vrsta == null) return;

        Dokument d = new Dokument();
        d.setNaziv(vrsta.getNaziv());
        d.setVrstaDokumenta(vrsta);
        d.setDatumUpload(LocalDate.now());
        d.setDostavljen(true);

        if (pdfBase64 != null) {
            d.setDokumentB64(pdfBase64);
        }

        new DokumentDAO().unesiDokument(d, prijavaId);

        cmbDokument.getSelectionModel().clearSelection();
        pdfBase64 = null;
        lblPdf.setText("");
    }
}
