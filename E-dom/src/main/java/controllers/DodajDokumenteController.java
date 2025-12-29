package controllers;

import controllers.DodajDokumenteControllers.*;
import dao.DokumentDAO;
import dao.PrijavaDAO;
import dao.VrstaDokumentaDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.BraniociRezultat;
import model.Dokument;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuSocijalnogStatusa;
import service.KriterijPoOsnovuUdaljenosti;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DodajDokumenteController {

    @FXML private int prijavaId;
    private int clanovi;
    private double udaljenost;
    private double prosjek;
    private int godinaStudija;
    private BraniociRezultat braniociRezultat;

    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();
    private final Map<Integer, Double> primanjaPoClanu = new HashMap<>();

    private VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();
    private final List<VrstaDokumenta> vrsteDokumenata = vdDao.dohvatiSveVrste();

    @FXML private Accordion accordionDokumenti;

    private KucnaListaController kucnaControllerRef;

    @FXML
    public void initialize() {
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
        dodajPrijavniObrazac(vboxOsnovni);
        dodajCipsSekciju(vboxOsnovni);
        dodajUplatnicuSekciju(vboxOsnovni);
        dodajGarancijuSekciju(vboxOsnovni);
        dodajKucnuListuDokument(vboxOsnovni);

        TitledPane paneDomacinstvo = new TitledPane();
        paneDomacinstvo.setText("Domaćinstvo");
        dodajKucnuListuSekciju(paneDomacinstvo);

        TitledPane paneFaks = new TitledPane();
        paneFaks.setText("Dokumenti sa fakulteta/škole");
        VBox vboxFaks = new VBox(10);
        paneFaks.setContent(vboxFaks);
        dodajSekcijuSvjedodzbe(vboxFaks);
        dodajNagradeSekciju(vboxFaks);

        accordionDokumenti.getPanes().addAll(paneOsnovni, paneDomacinstvo, paneFaks);

        // Prikaži sekciju za dodatne bodove samo ako postoje bodovi branilaca
        if (braniociRezultat != null && braniociRezultat.getBodovi() > 0) {
            TitledPane paneDodatni = new TitledPane();
            paneDodatni.setText("Dodatni bodovi");
            VBox vboxDodatni = new VBox(10);
            paneDodatni.setContent(vboxDodatni);
            dodajSekcijuDodatniBodovi(vboxDodatni);
            accordionDokumenti.getPanes().add(paneDodatni);
        }
    }

    @FXML
    private void onPodnesiPrijavu(javafx.event.ActionEvent event) {
        if (kucnaControllerRef != null) {
            kucnaControllerRef.zavrsiUnos();
            primanjaPoClanu.putAll(kucnaControllerRef.getPrimanjaPoClanu());
        }

        double ukupnaPrimanja = primanjaPoClanu.values().stream().mapToDouble(Double::doubleValue).sum();
        double primanjaPoClanuVrijednost = (clanovi == 0) ? 0 : ukupnaPrimanja / clanovi;
        int ukupniBodovi = KriterijPoOsnovuSocijalnogStatusa.bodoviZaPrimanja(primanjaPoClanuVrijednost);

        PrijavaDAO prijavaDAO = new PrijavaDAO();
        prijavaDAO.dodajBodoveNaPrijavu(prijavaId, ukupniBodovi);

        showAlert("Trenutni broj bodova: ",
                "Ukupna primanja: " + ukupnaPrimanja +
                        "\nPrimanja po članu: " + primanjaPoClanuVrijednost +
                        "\nUKUPNI BODOVI: " + ukupniBodovi
        );

        Stage stage = (Stage) accordionDokumenti.getScene().getWindow();
        stage.close();
    }

    public void setProsjek(double prosjek) {
        this.prosjek = prosjek;
    }

    public void setGodinaStudija(int godinaStudija) {
        this.godinaStudija = godinaStudija;
    }

    public void setUdaljenost(double udaljenost) {
        this.udaljenost = udaljenost;
        KriterijPoOsnovuUdaljenosti kriterij = new KriterijPoOsnovuUdaljenosti();
        double bodovi = kriterij.izracunajBodove(udaljenost);
        PrijavaDAO prijavaDAO = new PrijavaDAO();
        prijavaDAO.dodajBodoveNaPrijavu(prijavaId, bodovi);
    }

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public void setBraniociRezultat(BraniociRezultat rezultat) {
        this.braniociRezultat = rezultat;
        if (rezultat != null && rezultat.getBodovi() > 0) {
            PrijavaDAO prijavaDAO = new PrijavaDAO();
            prijavaDAO.dodajBodoveNaPrijavu(prijavaId, rezultat.getBodovi());
        }
    }

    public void setClanovi(int clanovi) {
        this.clanovi = clanovi;
        initAccordion();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/cips.fxml"));
            VBox cipsBox = loader.load();
            CipsDokumentController controller = loader.getController();
            controller.init(prijavaId, vdDao.dohvatiVrstuPoId(6));
            parent.getChildren().add(cipsBox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajUplatnicuSekciju(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/uplatnica.fxml"));
            VBox uplatnicaBox = loader.load();
            UplatnicaDokumentConroller controller = loader.getController();
            controller.init(prijavaId, vdDao.dohvatiVrstuPoId(1));
            parent.getChildren().add(uplatnicaBox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajNagradeSekciju(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/nagrade.fxml"));
            VBox nagradeBox = loader.load();
            NagradeDokumentController controller = loader.getController();
            controller.init(prijavaId, vdDao.dohvatiVrstuPoId(10));
            parent.getChildren().add(nagradeBox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajSekcijuSvjedodzbe(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/prosjek.fxml"));
            VBox box = loader.load();
            ProsjekDokumentController controller = loader.getController();
            controller.init(prijavaId, godinaStudija, prosjek,
                    vdDao.dohvatiVrstuPoId(7),
                    vdDao.dohvatiVrstuPoId(8),
                    vdDao.dohvatiVrstuPoId(9));
            parent.getChildren().add(box);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajKucnuListuSekciju(TitledPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/kucna-lista.fxml"));
            VBox root = loader.load();
            kucnaControllerRef = loader.getController();
            kucnaControllerRef.init(prijavaId, clanovi, vrsteDokumenata);
            pane.setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dodajSekcijuDodatniBodovi(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/dodatni-bodovi.fxml"));
            VBox box = loader.load();
            DodatniBodoviController controller = loader.getController();

            // Prosljeđivanje naziva dokumenta i bodova
            String nazivDokumenta = (braniociRezultat != null)
                    ? braniociRezultat.getNaziv()
                    : "";

            controller.init(prijavaId, nazivDokumenta, List.of(
                    vdDao.dohvatiVrstuPoId(19),
                    vdDao.dohvatiVrstuPoId(20),
                    vdDao.dohvatiVrstuPoId(21),
                    vdDao.dohvatiVrstuPoId(22),
                    vdDao.dohvatiVrstuPoId(23)
            ));
            parent.getChildren().add(box);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dodajGarancijuSekciju(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/garancija.fxml"));
            VBox box = loader.load();
            GarancijaDokumentController controller = loader.getController();

            List<VrstaDokumenta> vrsteDokumenata = Arrays.asList(
                    vdDao.dohvatiVrstuPoId(5),
                    vdDao.dohvatiVrstuPoId(16),
                    vdDao.dohvatiVrstuPoId(24)
            );

            controller.init(prijavaId, vrsteDokumenata);
            parent.getChildren().add(box);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dodajPrijavniObrazac(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/obrazac-prijava.fxml"));
            VBox prijavaBox = loader.load();
            ObrazacPrijaveController controller = loader.getController();
            controller.init(prijavaId, vdDao.dohvatiVrstuPoId(17));
            parent.getChildren().add(prijavaBox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dodajKucnuListuDokument(VBox parent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/kucna-dokument.fxml"));
            VBox kucnaBox = loader.load();
            DodajKucnuListuController controller = loader.getController();
            controller.init(prijavaId, vdDao.dohvatiVrstuPoId(18));
            parent.getChildren().add(kucnaBox);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}