package controllers;

import dao.DokumentDAO;
import dao.PrijavaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Dokument;
import model.Prijava;
import model.VrstaDokumenta;
import service.BodovanjeService;

import java.time.LocalDate;
import java.util.*;

public class DodajDokumenteController {

    @FXML
    private VBox vboxClanovi;

    private int prijavaId;
    private int clanovi;

    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();
    private final Map<Integer, Double> primanjaPoClanu = new HashMap<>();

    private final List<VrstaDokumenta> vrsteDokumenata = List.of(
            new VrstaDokumenta(1, "Potvrda o primanjima"),
            new VrstaDokumenta(2, "Ugovor o radu"),
            new VrstaDokumenta(3, "Penzijsko rješenje"),
            new VrstaDokumenta(4, "Potvrda o školovanju"),
            new VrstaDokumenta(5, "Potvrda sa biroa")
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
        dokumentiPoClanu.clear();
        primanjaPoClanu.clear();

        for (int i = 1; i <= clanovi; i++) {
            final int clanIndex = i;

            VBox clanBox = new VBox(8);
            Label lblClan = new Label("Član " + clanIndex);

            TextField txtImeClana = new TextField();
            txtImeClana.setPromptText("Ime / uloga (npr. majka)");

            TextField txtIznos = new TextField();
            txtIznos.setPromptText("Mjesečna primanja (KM)");

            // ✅ KLJUČNO: spremi primanja kad korisnik izađe iz polja (nije vezano za dokument)
            txtIznos.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (wasFocused && !isNowFocused) { // izgubio fokus
                    double iznos = parseIznosOrZero(txtIznos.getText());
                    primanjaPoClanu.put(clanIndex, iznos);
                }
            });

            ComboBox<VrstaDokumenta> cmbVrsta = new ComboBox<>();
            cmbVrsta.setItems(FXCollections.observableArrayList(vrsteDokumenata));
            cmbVrsta.setPromptText("Vrsta dokumenta");

            CheckBox chkDostavljen = new CheckBox("Dokument dostavljen");
            Button btnDodajDokument = new Button("Dodaj dokument");

            dokumentiPoClanu.put(clanIndex, new ArrayList<>());

            btnDodajDokument.setOnAction(e -> {

                if (!chkDostavljen.isSelected()) {
                    showAlert("Greška", "Označi da je dokument dostavljen.");
                    return;
                }

                if (cmbVrsta.getValue() == null) {
                    showAlert("Greška", "Odaberi vrstu dokumenta.");
                    return;
                }

                // (primanja se već spremaju kroz listener, ali ovdje još jednom “osiguramo”)
                primanjaPoClanu.put(clanIndex, parseIznosOrZero(txtIznos.getText()));

                String ime = txtImeClana.getText().isEmpty()
                        ? "Član " + clanIndex
                        : txtImeClana.getText();

                Dokument d = new Dokument();
                d.setNaziv(cmbVrsta.getValue().getNaziv() + " - " + ime);
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0); // dokumenti trenutno ne nose bodove dok ne implementiraš UI za dodatne kriterije
                d.setDostavljen(true);
                d.setVrstaDokumenta(cmbVrsta.getValue());
                d.setDokumentB64(null);

                new DokumentDAO().unesiDokument(d, prijavaId);
                dokumentiPoClanu.get(clanIndex).add(d);

                cmbVrsta.getSelectionModel().clearSelection();
                chkDostavljen.setSelected(false);
            });

            HBox hbox = new HBox(10, txtIznos, cmbVrsta, chkDostavljen, btnDodajDokument);
            clanBox.getChildren().addAll(lblClan, txtImeClana, hbox);
            vboxClanovi.getChildren().add(clanBox);
        }

        Button btnZavrsi = new Button("Završi unos");
        btnZavrsi.setOnAction(e -> zavrsiUnos());
        vboxClanovi.getChildren().add(btnZavrsi);
    }

    private void zavrsiUnos() {

        // ✅ uzmi primanja za sve članove; ako neko nije upisan -> tretiraj kao 0
        for (int i = 1; i <= clanovi; i++) {
            primanjaPoClanu.putIfAbsent(i, 0.0);
        }

        double ukupnaPrimanja = primanjaPoClanu.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        // ✅ ključno: dijeli sa ukupnim brojem članova, ne sa map.size
        double primanjaPoClanuVrijednost = (clanovi == 0) ? 0 : ukupnaPrimanja / clanovi;

        int ukupniBodovi = BodovanjeService.bodoviZaPrimanja(primanjaPoClanuVrijednost);

        PrijavaDAO prijavaDAO = new PrijavaDAO();
        Prijava prijava = prijavaDAO.dohvatiPrijavuPoId(prijavaId);
        prijava.setUkupniBodovi(ukupniBodovi);
        prijavaDAO.azurirajPrijavu(prijava);

        showAlert(
                "Trenutni broj bodova: ",
                "Ukupna primanja: " + ukupnaPrimanja +
                        "\nPrimanja po članu: " + primanjaPoClanuVrijednost +
                        "\nUKUPNI BODOVI: " + ukupniBodovi
        );

        Stage stage = (Stage) vboxClanovi.getScene().getWindow();
        stage.close();
    }

    private double parseIznosOrZero(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
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
