package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import model.Dokument;
import model.VrstaDokumenta;

import java.time.LocalDate;
import java.util.List;

public class DodatniBodoviController {

    @FXML
    private ComboBox<VrstaDokumenta> cmbDokument;

    private int prijavaId;

    public void init(int prijavaId, List<VrstaDokumenta> dokumenti) {
        this.prijavaId = prijavaId;
        cmbDokument.getItems().addAll(dokumenti);
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

        new DokumentDAO().unesiDokument(d, prijavaId);
    }
}
