package controllers;

import dao.DokumentDAO;
import dao.PrijavaDAO;
import dao.VrstaDokumentaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Dokument;
import model.Prijava;
import model.SocijalniStatus;
import model.VrstaDokumenta;
import service.BodovanjeService;
import service.KriterijPoOsnovuUspjeha;

import javax.swing.*;
import java.time.LocalDate;
import java.util.*;

public class DodajDokumenteController {

    @FXML
    private VBox vboxClanovi;

    private int prijavaId;
    private int clanovi;
    private double udaljenost;
    private double prosjek;
    private int godinaStudija;


    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();
    private final Map<Integer, Double> primanjaPoClanu = new HashMap<>();

    private KriterijPoOsnovuUspjeha kriterij = new KriterijPoOsnovuUspjeha();


    private VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();

    private final List<VrstaDokumenta> vrsteDokumenata = vdDao.dohvatiSveVrste();

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public void setClanovi(int clanovi) {
        this.clanovi = clanovi;
        generisiSekcijeZaClanove();
    }

    public void setProsjek(double prosjek) {this.prosjek=prosjek;}
    public void setGodinaStudija(int godinaStudija){
        this.godinaStudija=godinaStudija;
        if(godinaStudija == 1) {
            generisiSekciju(vdDao.dohvatiVrstuPoId(7));
        }
        else{
            generisiSekciju(vdDao.dohvatiVrstuPoId(8));
        }
    }

    public void setUdaljenost(double udaljenost){
        this.udaljenost=udaljenost;
        generisiSekciju(vdDao.dohvatiVrstuPoId(6));
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

            cmbVrsta.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(VrstaDokumenta vrsta, boolean empty){
                    super.updateItem(vrsta, empty);
                    setText((empty || vrsta == null) ? "" : vrsta.getNaziv());
                }
            });

            cmbVrsta.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(VrstaDokumenta vrsta, boolean empty){
                    super.updateItem(vrsta, empty);
                    setText((empty || vrsta == null) ? "" : vrsta.getNaziv());
                }
            });

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
            });

            HBox hbox = new HBox(10, txtIznos, cmbVrsta, chkDostavljen, btnDodajDokument);
            clanBox.getChildren().addAll(lblClan, txtImeClana, hbox);
            vboxClanovi.getChildren().add(clanBox);
        }

        Button btnZavrsi = new Button("Završi unos");
        btnZavrsi.setOnAction(e -> zavrsiUnos());
        vboxClanovi.getChildren().add(btnZavrsi);
    }

    private void generisiSekciju(VrstaDokumenta vrsta) {
        if("CIPS".equals(vrsta.getNaziv())){
            VBox cipsBox = new VBox(8);
            cipsBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-radius: 5;");

            Label lblCIPS = new Label("Unesi CIPS ");
            TextField txtNaziv = new TextField();


            CheckBox chkDostavljen = new CheckBox("Dokument dostavljen");
            Button btnDodajDokument = new Button("Dodaj dokument");

            btnDodajDokument.setOnAction(e -> {

                if (!chkDostavljen.isSelected()) {
                    showAlert("Greška", "Označi da je dokument dostavljen.");
                    return;
                }

                Dokument d = new Dokument();
                d.setNaziv("CIPS - potvrda o mjestu stanovanja");
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0); // dokumenti trenutno ne nose bodove dok ne implementiraš UI za dodatne kriterije
                d.setDostavljen(true);
                d.setVrstaDokumenta(vrsta);
                d.setDokumentB64(null);

                new DokumentDAO().unesiDokument(d, prijavaId);
            });

            cipsBox.getChildren().addAll(
                    lblCIPS,
                    chkDostavljen,
                    btnDodajDokument
            );

            vboxClanovi.getChildren().add(cipsBox);
        }
        else if("Svjedodzba o zavrsetku srednje skole".equals(vrsta.getNaziv())){
            VBox svjedozbaBox = new VBox(8);
            svjedozbaBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-radius: 5;");

            Label lblSvjedozba = new Label("Unesi svjedozbu o zavrsetku srednje skole:");
            TextField txtNaziv = new TextField();

            CheckBox chkDostavljen = new CheckBox("Dokument dostavljen");
            Button btnDodajDokument = new Button("Dodaj dokument");

            btnDodajDokument.setOnAction(e -> {
                if (!chkDostavljen.isSelected()) {
                    showAlert("Greška", "Označi da je dokument dostavljen.");
                    return;
                }

                Dokument d = new Dokument();
                d.setNaziv("Svjedozba o zavrsetku srednje skole");
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0);
                d.setDostavljen(true);
                d.setVrstaDokumenta(vrsta);
                d.setDokumentB64(null);

                new DokumentDAO().unesiDokument(d, prijavaId);

                kriterij.izracunajBodoveBrucosi(prijavaId, prosjek);
            });

            // Dodaj sve elemente u VBox
            svjedozbaBox.getChildren().addAll(
                    lblSvjedozba,
                    txtNaziv,
                    chkDostavljen,
                    btnDodajDokument
            );

            // Dodaj VBox u glavni container
            vboxClanovi.getChildren().add(svjedozbaBox);
        }
        else if("Uvjerenje o polozenim ispitima".equals(vrsta.getNaziv())){
            VBox prosjekBox = new VBox(8);
            prosjekBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray; -fx-border-radius: 5;");

            Label lblIspiti = new Label("Unesi uvjerenje o polozenim ispitima: ");

            CheckBox chkUvjerenje = new CheckBox("Uvjerenje dostavljeno");
            CheckBox chkIndeks = new CheckBox("Indeks dostavljen");

            TextField txtBrojPolozenih = new TextField();

            Button btnDodajDokument = new Button("Dodaj dokument");

            btnDodajDokument.setOnAction(e -> {
                VrstaDokumenta vrstaZaDokument; // nova lokalna varijabla

                String naziv;
                if(chkUvjerenje.isSelected()) {
                    naziv = "Uvjerenje o polozenim ispitima";
                    vrstaZaDokument = vrsta; // originalna vrsta
                } else {
                    naziv = "Ovjerena kopija indeksa sa ocjenama";
                    vrstaZaDokument = vdDao.dohvatiVrstuPoId(9); // nova vrsta
                }

                Dokument d = new Dokument();
                d.setNaziv(naziv);
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0);
                d.setDostavljen(true);
                d.setVrstaDokumenta(vrstaZaDokument);
                d.setDokumentB64(null);

                new DokumentDAO().unesiDokument(d, prijavaId);

                int brojPolozenih = parseIznosInt(txtBrojPolozenih.getText());
                kriterij.izracunajBodove(prijavaId, prosjek, brojPolozenih, godinaStudija );
            });


            prosjekBox.getChildren().addAll(
                    lblIspiti,
                    chkUvjerenje,
                    chkIndeks,
                    txtBrojPolozenih,
                    btnDodajDokument
            );

            vboxClanovi.getChildren().add(prosjekBox);
        }
    }

    private int parseIznosInt(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
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
