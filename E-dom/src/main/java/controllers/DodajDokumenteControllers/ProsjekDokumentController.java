package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import dao.PrijavaDAO;
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
        // Povezivanje radio buttona u grupu da se može odabrati samo jedan
        rbUvjerenje.setToggleGroup(toggleDokument);
        rbIndeks.setToggleGroup(toggleDokument);
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

        // Prikaz interfejsa zavisno od godine studija
        if (godinaStudija == 1) {
            lblOpis.setText("Svjedodžba o završetku srednje škole:");
            setNodeVisible(lblIspiti, false);
            setNodeVisible(txtBrojPolozenih, false);
            setNodeVisible(rbUvjerenje, false);
            setNodeVisible(rbIndeks, false);
        } else {
            lblOpis.setText("Odaberi i unesi dokument sa fakulteta:");
            setNodeVisible(lblIspiti, true);
            setNodeVisible(txtBrojPolozenih, true);
            setNodeVisible(rbUvjerenje, true);
            setNodeVisible(rbIndeks, true);
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

        double bodovi = 0;

        // --- LOGIKA ---
        if (godinaStudija == 1) {
            // 1. GODINA (Brucoši)
            bodovi = kriterij.izracunajBodoveBrucosi(prosjek);

            d.setNaziv(vrstaSvjedodzbe.getNaziv());
            d.setVrstaDokumenta(vrstaSvjedodzbe);

        } else {
            // VIŠE GODINE (Stariji studenti)

            // Provjera šta je selektovano (Indeks ili Uvjerenje)
            if (rbUvjerenje.isSelected()) {
                d.setNaziv(vrstaUvjerenje.getNaziv());
                d.setVrstaDokumenta(vrstaUvjerenje);
            } else if (rbIndeks.isSelected()) {
                d.setNaziv(vrstaIndeks.getNaziv());
                d.setVrstaDokumenta(vrstaIndeks);
            } else {
                showAlert("Greška", "Morate odabrati vrstu dokumenta.");
                return;
            }

            int brojPolozenih = parseIntOrZero(txtBrojPolozenih.getText());
            if (brojPolozenih <= 0) {
                showAlert("Greška", "Unesite validan broj položenih ispita.");
                return;
            }

            // Računanje bodova za starije godine
            // Ovdje koristim metodu koja prima (godina, prosjek, brojPolozenih)
            bodovi = kriterij.izracunajBodove(godinaStudija, prosjek, brojPolozenih);
        }

        // --- ZAJEDNIČKO SNIMANJE ---
        d.setBrojBodova(bodovi);

        // 1. Spasi dokument u bazu
        new DokumentDAO().unesiDokument(d, prijavaId);

        // 2. Ažuriraj ukupne bodove na prijavi (ovo je falilo u nekim verzijama)
        new PrijavaDAO().dodajBodoveNaPrijavu(prijavaId, bodovi);

        showAlert("Uspješno", "Dokument dodat i bodovi (" + String.format("%.2f", bodovi) + ") obračunati.");
    }

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