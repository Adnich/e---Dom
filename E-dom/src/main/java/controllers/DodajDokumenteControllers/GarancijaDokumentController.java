package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import dao.VrstaDokumentaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Dokument;
import model.VrstaDokumenta;
import service.PdfUpload;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GarancijaDokumentController {

    @FXML private TextField txtImeGaranta;
    @FXML private ComboBox<String> cmbTipGaranta;
    @FXML private CheckBox chkGarancijaPotpisana;
    @FXML private ComboBox<VrstaDokumenta> cmbDokumentGarancija;
    @FXML private Button btnDodaj;


    @FXML private Label lblPdf;
    private String pdfBase64;

    private int prijavaId;
    private VrstaDokumenta vrsta;
    private final VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();

    public void init(int prijavaId, VrstaDokumenta vrsta) {
        this.prijavaId = prijavaId;
        this.vrsta = vrsta;

        cmbTipGaranta.setItems(FXCollections.observableArrayList(
                "Zaposlenik",
                "Penzioner",
                "Vlasnik kompanije"
        ));

        cmbDokumentGarancija.setItems(FXCollections.observableArrayList());

        cmbTipGaranta.setOnAction(e -> filtrirajDokumenteZaTipGaranta());
    }

    private void filtrirajDokumenteZaTipGaranta() {
        String tip = cmbTipGaranta.getValue();
        if (tip == null) {
            cmbDokumentGarancija.getItems().clear();
            return;
        }

        List<VrstaDokumenta> svi = vdDao.dohvatiSveVrste();

        List<VrstaDokumenta> filtrirani = switch (tip) {
            case "Zaposlenik" -> svi.stream()
                    .filter(v -> v.getIdVrsta() == 1)
                    .collect(Collectors.toList());
            case "Penzioner" -> svi.stream()
                    .filter(v -> v.getIdVrsta() == 2)
                    .collect(Collectors.toList());
            case "Vlasnik kompanije" -> svi.stream()
                    .filter(v -> v.getIdVrsta() == 3 || v.getIdVrsta() == 4)
                    .collect(Collectors.toList());
            default -> List.of();
        };

        cmbDokumentGarancija.setItems(FXCollections.observableArrayList(filtrirani));
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
    private void dodajDokumentGarancija() {

        if (txtImeGaranta.getText().isEmpty()
                || cmbTipGaranta.getValue() == null
                || !chkGarancijaPotpisana.isSelected()
                || cmbDokumentGarancija.getValue() == null) {

            new Alert(
                    Alert.AlertType.WARNING,
                    "Popunite ime garanta, tip, ovjeru i odaberite dokument."
            ).showAndWait();
            return;
        }

        Dokument d = new Dokument();
        d.setNaziv("Garancija - " + cmbDokumentGarancija.getValue().getNaziv()
                + " (" + txtImeGaranta.getText() + ")");
        d.setDatumUpload(LocalDate.now());
        d.setDostavljen(true);
        d.setVrstaDokumenta(cmbDokumentGarancija.getValue());

        // PDF JE OPCIONALAN
        if (pdfBase64 != null) {
            d.setDokumentB64(pdfBase64);
        }

        new DokumentDAO().unesiDokument(d, prijavaId);

        new Alert(Alert.AlertType.INFORMATION,
                "Garancija uspješno dodana!"
        ).showAndWait();
    }
}
