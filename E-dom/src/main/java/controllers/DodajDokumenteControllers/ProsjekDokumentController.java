package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import model.Dokument;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuUspjeha;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

public class ProsjekDokumentController {
    @FXML
    private Label lblIspiti;
    @FXML
    private VBox rootBox;

    @FXML
    private Label lblOpis;

    @FXML
    private TextField txtBrojPolozenih; // za uvjerenje/indeks

    @FXML
    private CheckBox chkDostavljen;

    @FXML
    private RadioButton rbUvjerenje; // opcionalno, samo za višu godinu
    @FXML
    private RadioButton rbIndeks;

    private int prijavaId;
    private double prosjek;
    private int godinaStudija;
    private VrstaDokumenta vrstaSvjedodzbe;
    private VrstaDokumenta vrstaUvjerenje;
    private VrstaDokumenta vrstaIndeks;

    private final KriterijPoOsnovuUspjeha kriterij = new KriterijPoOsnovuUspjeha();

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
            // prikazuje samo svjedodžbu iz srednje škole
            lblOpis.setText("Svjedodžba o završetku srednje škole:");
            lblIspiti.setVisible(false);
            txtBrojPolozenih.setVisible(false);
            rbUvjerenje.setVisible(false);
            rbIndeks.setVisible(false);
        } else {
            // viša godina → prikazuje opciju uvjerenje / indeks
            lblOpis.setText("Odaberi i unesi dokument sa fakulteta:");
            lblIspiti.setVisible(true);
            txtBrojPolozenih.setVisible(true);
            rbUvjerenje.setVisible(true);
            rbIndeks.setVisible(true);
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
        d.setBrojBodova(0);
        d.setDokumentB64(null);

        if (godinaStudija == 1) {
            // brucosi → svjedodžba iz srednje
            d.setNaziv(vrstaSvjedodzbe.getNaziv());
            d.setVrstaDokumenta(vrstaSvjedodzbe);
            new DokumentDAO().unesiDokument(d, prijavaId);
            kriterij.izracunajBodoveBrucosi(prijavaId, prosjek);

        } else {
            // viša godina
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

            new DokumentDAO().unesiDokument(d, prijavaId);
            int brojPolozenih = parseIntOrZero(txtBrojPolozenih.getText());
            kriterij.izracunajBodove(prijavaId, prosjek, brojPolozenih, godinaStudija);
        }

        showAlert("Uspješno", "Dokument dodat i bodovi obračunati.");
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
