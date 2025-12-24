package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import model.Dokument;
import model.VrstaDokumenta;
import service.PdfService;

import java.time.LocalDate;

public class DodajKucnuListuController {
    @FXML
    private CheckBox chkDostavljen;

    @FXML
    private Label lblPdf;

    private int prijavaId;
    private VrstaDokumenta vrsta;

    private String pdfBase64; // ide u DokumentB64

    public void init(int prijavaId, VrstaDokumenta vrsta) {
        this.prijavaId = prijavaId;
        this.vrsta = vrsta;
    }

    // dugme: Dodaj PDF (nije obavezno)
    @FXML
    private void onDodajPdf() {
        pdfBase64 = PdfService.uploadPdf(lblPdf.getScene().getWindow());

        if (pdfBase64 != null) {
            lblPdf.setText("PDF dodat");
        } else {
            lblPdf.setText("PDF nije dodat");
        }
    }

    // dugme: Sačuvaj dokument
    @FXML
    private void dodaj() {

        if (!chkDostavljen.isSelected()) {
            return; // admin nije označio → ne snimamo
        }

        Dokument d = new Dokument();
        d.setNaziv("Uvjerenje o zajedničkom domaćinstvu");
        d.setDatumUpload(LocalDate.now());
        d.setDostavljen(true);
        d.setVrstaDokumenta(vrsta);

        // PDF je OPCIONALAN
        if (pdfBase64 != null) {
            d.setDokumentB64(pdfBase64);
        }

        new DokumentDAO().unesiDokument(d, prijavaId);
    }
}
