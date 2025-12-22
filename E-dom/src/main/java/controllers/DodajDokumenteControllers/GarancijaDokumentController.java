package controllers.DodajDokumenteControllers;

import dao.VrstaDokumentaDAO;
import dao.DokumentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Dokument;
import model.VrstaDokumenta;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GarancijaDokumentController {

    @FXML private TextField txtImeGaranta;
    @FXML private ComboBox<String> cmbTipGaranta;
    @FXML private CheckBox chkGarancijaPotpisana;
    @FXML private ComboBox<VrstaDokumenta> cmbDokumentGarancija;
    @FXML private Button btnDodaj;

    private int prijavaId;
    private VrstaDokumenta vrsta; // osnovna vrsta dokumenta "Garancija"
    private final VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();

    public void init(int prijavaId, VrstaDokumenta vrsta) {
        this.prijavaId = prijavaId;
        this.vrsta = vrsta;

        // tip garanta
        cmbTipGaranta.setItems(FXCollections.observableArrayList(
                "Zaposlenik", "Penzioner", "Vlasnik kompanije"
        ));

        // inicijalno prazan ComboBox za dokumente
        cmbDokumentGarancija.setItems(FXCollections.observableArrayList());

        // promjena tipa garanta -> filtrira dokumente
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
                    .filter(v -> v.getIdVrsta() == 1) // npr. 1 = Potvrda o stalnom radu
                    .collect(Collectors.toList());
            case "Penzioner" -> svi.stream()
                    .filter(v -> v.getIdVrsta() == 2) // 2 = tri posljednja čeka penzije
                    .collect(Collectors.toList());
            case "Vlasnik kompanije" -> svi.stream()
                    .filter(v -> v.getIdVrsta() == 3 || v.getIdVrsta() == 4) // 3 = pečat+potpis, 4 = ID/PDV
                    .collect(Collectors.toList());
            default -> List.of();
        };

        cmbDokumentGarancija.setItems(FXCollections.observableArrayList(filtrirani));
    }

    @FXML
    private void dodajDokumentGarancija() {
        if (txtImeGaranta.getText().isEmpty() ||
                cmbTipGaranta.getValue() == null ||
                !chkGarancijaPotpisana.isSelected() ||
                cmbDokumentGarancija.getValue() == null) {

            Alert a = new Alert(Alert.AlertType.WARNING,
                    "Popunite ime garanta, tip, ovjeru i odaberite dokument.");
            a.showAndWait();
            return;
        }

        Dokument d = new Dokument();
        d.setNaziv("Garancija - " + cmbDokumentGarancija.getValue().getNaziv());
        d.setDatumUpload(LocalDate.now());
        d.setDostavljen(true);
        d.setVrstaDokumenta(cmbDokumentGarancija.getValue());

        new DokumentDAO().unesiDokument(d, prijavaId);

        Alert a = new Alert(Alert.AlertType.INFORMATION, "Dokument uspješno dodan!");
        a.showAndWait();
    }
}
