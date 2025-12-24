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


    @FXML
    private Accordion accordionDokumenti;

    @FXML
    public void initialize() {
        // automatsko podešavanje prozora
        accordionDokumenti.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setWidth(900);
                stage.setHeight(700);
                stage.centerOnScreen();
            }
        });
    }

    private void initAccordion() {

        accordionDokumenti.getPanes().clear();

        if (clanovi <= 0) {
            System.out.println("⚠️ clanovi još nisu postavljeni");
            return;
        }

        TitledPane paneOsnovni = new TitledPane();
        paneOsnovni.setText("Osnovni dokumenti");
        VBox vboxOsnovni = new VBox(10);
        paneOsnovni.setContent(vboxOsnovni);
        dodajCipsSekciju(vboxOsnovni);
        dodajUplatnicuSekciju(vboxOsnovni);
        dodajGarancijuSekciju(vboxOsnovni);


        TitledPane paneDomacinstvo = new TitledPane();
        paneDomacinstvo.setText("Domaćinstvo");
        dodajKucnuListuSekciju(paneDomacinstvo);



        TitledPane paneFaks = new TitledPane();
        paneFaks.setText("Dokumenti sa fakulteta/škole");
        VBox vboxFaks = new VBox(10);
        paneFaks.setContent(vboxFaks);
        dodajSekcijuSvjedodzbe(vboxFaks);
        dodajNagradeSkeciju(vboxFaks);

        // OSTALO
        TitledPane paneDodatni = new TitledPane();
        paneDodatni.setText("Ostalo");
        VBox vboxDodatni = new VBox(10);
        paneDodatni.setContent(vboxDodatni);
        dodajSekcijuDodatniBodovi(vboxDodatni);

        // dodaj sve TitledPane u Accordion
        accordionDokumenti.getPanes().addAll(paneDomacinstvo, paneOsnovni, paneFaks, paneDodatni);
    }



    @FXML
    private void onPodnesiPrijavu(javafx.event.ActionEvent event) {
        zavrsiUnos();
    }


    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public void setClanovi(int clanovi) {
        this.clanovi = clanovi;
        initAccordion();
    }

    public void setProsjek(double prosjek) { this.prosjek = prosjek; }

    public void setGodinaStudija(int godinaStudija){
        this.godinaStudija = godinaStudija;
    }

    public void setUdaljenost(double udaljenost){
        this.udaljenost = udaljenost;
    }

    public void setBodoviBranioci(int bodoviBranioci) {
        this.bodoviBranioci = bodoviBranioci;

        if (bodoviBranioci > 0) {
            //dodajSekcijuDodatniBodovi();
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

        Stage stage = (Stage) accordionDokumenti.getScene().getWindow();
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

    private void dodajCipsSekciju(VBox parent) {
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

            parent.getChildren().add(cipsBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajUplatnicuSekciju(VBox parent){
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

            parent.getChildren().add(uplatnicaBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajNagradeSkeciju(VBox parent){
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
            parent.getChildren().add(NagradeBox);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajSekcijuSvjedodzbe(VBox parent) {
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

            parent.getChildren().add(box);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void dodajKucnuListuSekciju(TitledPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/kucna-lista.fxml"));
            VBox root = loader.load();  // root VBox iz FXML-a

            // Dohvati kontroler i inicijaliziraj ga s podacima
            KucnaListaController controller = loader.getController();
            controller.init(prijavaId, clanovi, vrsteDokumenata);

            // Stavimo učitani root direktno kao content TitledPane-a
            pane.setContent(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void dodajSekcijuDodatniBodovi(VBox parent) {
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

            parent.getChildren().add(box);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void dodajGarancijuSekciju(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/garancija.fxml")
            );

            VBox box = loader.load();

            GarancijaDokumentController controller = loader.getController();
            controller.init(
                    prijavaId,
                    vdDao.dohvatiVrstuPoId(13) // npr. ID za garanciju
            );

            parent.getChildren().add(box);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
