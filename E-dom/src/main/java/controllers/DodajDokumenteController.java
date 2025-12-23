package controllers;

import controllers.DodajDokumenteControllers.*;
import dao.DokumentDAO;
import dao.PrijavaDAO;
import dao.VrstaDokumentaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Dokument;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuSocijalnogStatusa;
import service.KriterijPoOsnovuUdaljenosti;
import service.KriterijPoOsnovuUspjeha;

import java.io.IOException;
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
    private int bodoviBranioci;

    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();
    private final Map<Integer, Double> primanjaPoClanu = new HashMap<>();

    private KriterijPoOsnovuUspjeha kriterij = new KriterijPoOsnovuUspjeha();

    private VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();

    private final List<VrstaDokumenta> vrsteDokumenata = vdDao.dohvatiSveVrste();

    // ✅ DODANO: automatsko podešavanje veličine prozora kad se FXML učita i Stage postane dostupan
    @FXML
    public void initialize() {
        vboxClanovi.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();

                // Ovdje promijeni dimenzije kako želiš:
                stage.setWidth(900);
                stage.setHeight(700);

                stage.centerOnScreen(); // opcionalno
            }
        });
    }

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public void setClanovi(int clanovi) {
        this.clanovi = clanovi;
        dodajKucnuListuSekciju();
    }

    public void setProsjek(double prosjek) { this.prosjek = prosjek; }

    public void setGodinaStudija(int godinaStudija){
        this.godinaStudija = godinaStudija;
        dodajSekcijuSvjedodzbe();
    }

    public void setUdaljenost(double udaljenost){
        this.udaljenost = udaljenost;
        dodajCipsSekciju();
        dodajUplatnicuSekciju();
        dodajNagradeSkeciju();
    }

    public void setBodoviBranioci(int bodoviBranioci) {
        this.bodoviBranioci = bodoviBranioci;

        if (bodoviBranioci > 0) {
            dodajSekcijuDodatniBodovi();
        }
        PrijavaDAO prijavaDAO = new PrijavaDAO();
        prijavaDAO.dodajBodoveNaPrijavu(prijavaId, bodoviBranioci);
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
        for (int i = 1; i <= clanovi; i++) {
            primanjaPoClanu.putIfAbsent(i, 0.0);
        }

        double ukupnaPrimanja = primanjaPoClanu.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double primanjaPoClanuVrijednost = (clanovi == 0) ? 0 : ukupnaPrimanja / clanovi;

        int ukupniBodovi = KriterijPoOsnovuSocijalnogStatusa.bodoviZaPrimanja(primanjaPoClanuVrijednost);

        PrijavaDAO prijavaDAO = new PrijavaDAO();
        prijavaDAO.dodajBodoveNaPrijavu(prijavaId, ukupniBodovi);

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

    private void dodajCipsSekciju() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/cips.fxml")
            );

            VBox cipsBox = loader.load();

            CipsDokumentController controller = loader.getController();
            controller.init(
                    prijavaId,
                    vdDao.dohvatiVrstuPoId(6) // CIPS
            );

            vboxClanovi.getChildren().add(cipsBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajUplatnicuSekciju(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/uplatnica.fxml")
            );
            VBox uplatnicaBox = loader.load();

            UplatnicaDokumentConroller controller = loader.getController();
            controller.init(
                    prijavaId,
                    vdDao.dohvatiVrstuPoId( 1)
            );

            vboxClanovi.getChildren().add(uplatnicaBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajNagradeSkeciju(){
        try{
            FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/views/DodajDokumenteSections/nagrade.fxml")
            );

            VBox NagradeBox = loader.load();

            NagradeDokumentController controller = loader.getController();
            controller.init(
                    prijavaId,
                    vdDao.dohvatiVrstuPoId( 10)
            );
            vboxClanovi.getChildren().add(NagradeBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajSekcijuSvjedodzbe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/views/DodajDokumenteSections/prosjek.fxml"
            ));
            VBox box = loader.load();

            ProsjekDokumentController controller = loader.getController();
            controller.init(
                    prijavaId,
                    godinaStudija,
                    prosjek,
                    vdDao.dohvatiVrstuPoId(7),  // svjedodžba srednja
                    vdDao.dohvatiVrstuPoId(8),  // uvjerenje fakultet
                    vdDao.dohvatiVrstuPoId(9)   // indeks
            );

            vboxClanovi.getChildren().add(box);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void dodajKucnuListuSekciju() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/kucna-lista.fxml")
            );

            VBox box = loader.load();

            KucnaListaController controller = loader.getController();
            controller.init(
                    prijavaId,
                    clanovi,
                    vrsteDokumenata
            );

            vboxClanovi.getChildren().add(box);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajSekcijuDodatniBodovi() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/dodatni-bodovi.fxml")
            );

            VBox box = loader.load();

            DodatniBodoviController controller = loader.getController();
            controller.init(
                    prijavaId,
                    List.of(
                            vdDao.dohvatiVrstuPoId(11), // npr. dokaz branioca
                            vdDao.dohvatiVrstuPoId(12)  // drugi dokument
                    )
            );

            vboxClanovi.getChildren().add(box);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
