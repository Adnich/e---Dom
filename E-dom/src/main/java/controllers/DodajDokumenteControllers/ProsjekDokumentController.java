package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Dokument;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuUspjeha;

import java.time.LocalDate;

public class ProsjekDokumentController {

    @FXML private Label lblIspiti;
    @FXML private Label lblOpis;
    @FXML private TextField txtBrojPolozenih;
    @FXML private CheckBox chkDostavljen;
    @FXML private RadioButton rbUvjerenje;
    @FXML private RadioButton rbIndeks;

    private final ToggleGroup toggleDokument = new ToggleGroup();

    private int prijavaId;
    private double prosjek;
    private int godinaStudija;

    private VrstaDokumenta vrstaSvjedodzbe;
    private VrstaDokumenta vrstaUvjerenje;
    private VrstaDokumenta vrstaIndeks;

    private final KriterijPoOsnovuUspjeha kriterij = new KriterijPoOsnovuUspjeha();

    @FXML
    public void initialize() {
        // ✅ ToggleGroup popravlja izgled i ponašanje RadioButton-a
        rbUvjerenje.setToggleGroup(toggleDokument);
        rbIndeks.setToggleGroup(toggleDokument);

        // default stanje
        rbUvjerenje.setSelected(true);
    }

    public void init(int prijavaId, int godinaStudija, double prosjek,
                     VrstaDokumenta vrstaSvjedodzbe,
                     VrstaDokumenta vrstaUvjerenje,
                     VrstaDokumenta vrstaIndeks) {

        this.prijavaId = prijavaId;
        this.godinaStudija = godinaStudija;
        this.prosjek = prosjek;
        this.vrstaSvjedodzbe = vrstaSvjedodzbe;
        this.vrstaUvjerenje = vrstaUvjerenje;
        this.vrstaIndeks = vrstaIndeks;

        if (godinaStudija == 1) {
            // ✅ prikazuje samo svjedodžbu
            lblOpis.setText("Svjedodžba o završetku srednje škole:");

            setNodeVisible(lblIspiti, false);
            setNodeVisible(txtBrojPolozenih, false);
            setNodeVisible(rbUvjerenje, false);
            setNodeVisible(rbIndeks, false);

        } else {
            // ✅ viša godina → uvjerenje / indeks
            lblOpis.setText("Odaberi i unesi dokument sa fakulteta:");

            setNodeVisible(lblIspiti, true);
            setNodeVisible(txtBrojPolozenih, true);
            setNodeVisible(rbUvjerenje, true);
            setNodeVisible(rbIndeks, true);

            // default
            rbUvjerenje.setSelected(true);
        }
    }

    @FXML
    private void dodaj() {
        if (!chkDostavljen.isSelected()) {
            showAlert("Greška", "Dokument mora biti dostavljen.");
            return;
        }

        Dokument d = new Dokument();
        d.setDatumUpload(LocalDate.now());
        d.setDostavljen(true);
        d.setDokumentB64(null);

        if (godinaStudija == 1) {
            // ✅ brucosi → svjedodžba
            double bodovi = kriterij.izracunajBodoveBrucosi(prijavaId, prosjek);

            d.setNaziv(vrstaSvjedodzbe.getNaziv());
            d.setVrstaDokumenta(vrstaSvjedodzbe);
            d.setBrojBodova(bodovi);

            new DokumentDAO().unesiDokument(d, prijavaId);

        } else {
            // ✅ viša godina → uvjerenje / indeks
            if (rbUvjerenje.isSelected()) {
                d.setNaziv(vrstaUvjerenje.getNaziv());
                d.setVrstaDokumenta(vrstaUvjerenje);

            } else if (rbIndeks.isSelected()) {
                d.setNaziv(vrstaIndeks.getNaziv());
                d.setVrstaDokumenta(vrstaIndeks);

            } else {
                showAlert("Greška", "Odaberi dokument: uvjerenje ili indeks.");
                return;
            }

            int brojPolozenih = parseIntOrZero(txtBrojPolozenih.getText());
            double bodovi = kriterij.izracunajBodove(prijavaId, prosjek, brojPolozenih, godinaStudija);

            d.setBrojBodova(bodovi);
            new DokumentDAO().unesiDokument(d, prijavaId);
        }

        showAlert("Uspješno", "Dokument dodat i bodovi obračunati.");
    }

    // ✅ ključna metoda za UI: visible + managed
    private void setNodeVisible(Control node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private int parseIntOrZero(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
