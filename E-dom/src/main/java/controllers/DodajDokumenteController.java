package controllers;

import dao.DokumentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Dokument;
import model.VrstaDokumenta;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class DodajDokumenteController {

    @FXML private VBox vboxClanovi;

    private int prijavaId;
    private int clanovi;

    private Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();
    private List<VrstaDokumenta> vrsteDokumenata = List.of(
            new VrstaDokumenta(1, "Potvrda o primanjima"),
            new VrstaDokumenta(2, "Ugovor o radu"),
            new VrstaDokumenta(3, "Penzijsko rješenje")
    );

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public void setClanovi(int clanovi) {
        this.clanovi = clanovi;
        generisiSekcijeZaClanove();
    }

    private void generisiSekcijeZaClanove() {
        vboxClanovi.getChildren().clear();

        for (int i = 1; i <= clanovi; i++) {
            final int clanIndex = i; // ovo je final i može se koristiti u lambdi

            VBox clanBox = new VBox();
            clanBox.setSpacing(5);

            Label lblClan = new Label("Član " + clanIndex + ":");

            CheckBox chkDostavljen = new CheckBox("Dokument dostavljen");
            TextField txtBodovi = new TextField();
            txtBodovi.setPromptText("Broj bodova");

            ComboBox<VrstaDokumenta> cmbVrsta = new ComboBox<>();
            cmbVrsta.setItems(FXCollections.observableArrayList(vrsteDokumenata));

            Button btnDodajDokument = new Button("Dodaj dokument");

            // inicijalizacija liste dokumenata za ovog člana
            dokumentiPoClanu.put(clanIndex, new ArrayList<>());

            int finalI = i;
            btnDodajDokument.setOnAction(e -> {
                if (!chkDostavljen.isSelected()) {
                    showAlert("Greška", "Morate označiti da je dokument dostavljen.");
                    return;
                }

                VrstaDokumenta vrsta = cmbVrsta.getValue();
                if (vrsta == null) {
                    showAlert("Greška", "Morate odabrati vrstu dokumenta.");
                    return;
                }

                int bodovi;
                try {
                    bodovi = Integer.parseInt(txtBodovi.getText());
                } catch (Exception ex) {
                    showAlert("Greška", "Broj bodova mora biti broj.");
                    return;
                }

                DokumentDAO dokumentDAO = new DokumentDAO();

                // Kreiramo dokument **bez potrebe za file**
                Dokument dokument = new Dokument();
                dokument.setNaziv("Potvrda o primanju " + (clanIndex) + ". člana domaćinstva");
                dokument.setDatumUpload(LocalDate.now());
                dokument.setBrojBodova(bodovi);
                dokument.setDostavljen(true);
                dokument.setVrstaDokumenta(vrsta);
                dokument.setDokumentB64(null); // file još nije dodan

                dokumentDAO.unesiDokument(dokument,prijavaId);

                dokumentiPoClanu.get(clanIndex).add(dokument);

                // Reset polja
                txtBodovi.clear();
                cmbVrsta.getSelectionModel().clearSelection();
                chkDostavljen.setSelected(false);
            });


            HBox hbox = new HBox(10, chkDostavljen, cmbVrsta, txtBodovi, btnDodajDokument);
            clanBox.getChildren().addAll(lblClan, hbox);

            vboxClanovi.getChildren().add(clanBox);
        }

        // dugme za završetak unosa
        Button btnZavrsi = new Button("Završi unos");
        btnZavrsi.setOnAction(e -> zavrsiUnos());
        vboxClanovi.getChildren().add(btnZavrsi);
    }

    private void zavrsiUnos() {
        int ukupnoDokumenata = dokumentiPoClanu.values().stream().mapToInt(List::size).sum();
        showAlert("Gotovo", "Dodavanje dokumenata završeno. Ukupno dokumenata: " + ukupnoDokumenata);

        Stage stage = (Stage) vboxClanovi.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
