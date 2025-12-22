package controllers;

import controllers.DodajDokumenteControllers.*;
import dao.PrijavaDAO;
import dao.VrstaDokumentaDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Dokument;
import model.StatusPrijave;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuSocijalnogStatusa;
import service.KriterijPoOsnovuUspjeha;

import java.io.IOException;
import java.util.*;

public class DodajDokumenteController {

    @FXML
    private VBox rootPane;

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

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private final VrstaDokumentaDAO vdDao = new VrstaDokumentaDAO();

    private final List<VrstaDokumenta> vrsteDokumenata = vdDao.dohvatiSveVrste();

    private final KriterijPoOsnovuUspjeha kriterij = new KriterijPoOsnovuUspjeha();

    @FXML
    public void initialize() {
        vboxClanovi.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.setWidth(900);
                stage.setHeight(700);
                stage.centerOnScreen();
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

    public void setProsjek(double prosjek) {
        this.prosjek = prosjek;
    }

    public void setGodinaStudija(int godinaStudija) {
        this.godinaStudija = godinaStudija;
        dodajSekcijuSvjedodzbe();
    }

    public void setUdaljenost(double udaljenost) {
        this.udaljenost = udaljenost;
        dodajCipsSekciju();
        dodajUplatnicuSekciju();
        dodajNagradeSekciju();
    }

    public void setBodoviBranioci(int bodoviBranioci) {
        this.bodoviBranioci = bodoviBranioci;

        if (bodoviBranioci > 0) {
            dodajSekcijuDodatniBodovi();
            prijavaDAO.dodajBodoveNaPrijavu(prijavaId, bodoviBranioci);
        }
    }

    private void dodajCipsSekciju() {
        ucitajSekciju("/views/DodajDokumenteSections/cips.fxml",
                c -> ((CipsDokumentController) c)
                        .init(prijavaId, vdDao.dohvatiVrstuPoId(6)));
    }

    private void dodajUplatnicuSekciju() {
        ucitajSekciju("/views/DodajDokumenteSections/uplatnica.fxml",
                c -> ((UplatnicaDokumentConroller) c)
                        .init(prijavaId, vdDao.dohvatiVrstuPoId(1)));
    }

    private void dodajNagradeSekciju() {
        ucitajSekciju("/views/DodajDokumenteSections/nagrade.fxml",
                c -> ((NagradeDokumentController) c)
                        .init(prijavaId, vdDao.dohvatiVrstuPoId(10)));
    }

    private void dodajSekcijuSvjedodzbe() {
        ucitajSekciju("/views/DodajDokumenteSections/prosjek.fxml",
                c -> ((ProsjekDokumentController) c).init(
                        prijavaId,
                        godinaStudija,
                        prosjek,
                        vdDao.dohvatiVrstuPoId(7),
                        vdDao.dohvatiVrstuPoId(8),
                        vdDao.dohvatiVrstuPoId(9)
                ));
    }

    private void dodajKucnuListuSekciju() {
        ucitajSekciju("/views/DodajDokumenteSections/kucna-lista.fxml",
                c -> ((KucnaListaController) c)
                        .init(prijavaId, clanovi, vrsteDokumenata));
    }

    private void dodajSekcijuDodatniBodovi() {
        ucitajSekciju("/views/DodajDokumenteSections/dodatni-bodovi.fxml",
                c -> ((DodatniBodoviController) c).init(
                        prijavaId,
                        List.of(
                                vdDao.dohvatiVrstuPoId(11),
                                vdDao.dohvatiVrstuPoId(12)
                        )));
    }

    private void ucitajSekciju(String fxml, ControllerInit init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            VBox box = loader.load();
            init.init(loader.getController());
            vboxClanovi.getChildren().add(box);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private interface ControllerInit {
        void init(Object controller);
    }

    @FXML
    private void onPodnesiPrijavu() {

        prijavaDAO.promijeniStatusPrijave(
                prijavaId,
                new StatusPrijave(2, "Podnesena")
        );

        showAlert("Uspjeh", "Prijava je uspješno podnesena.");

        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/prijave.fxml")
            );
            Scene scene = new Scene(loader.load());

            prijaveController controller = loader.getController();
            controller.refreshTabela();

            Stage prijaveStage = new Stage();
            prijaveStage.setTitle("Prijave");
            prijaveStage.setScene(scene);
            prijaveStage.show();

        } catch (Exception e) {
            e.printStackTrace();
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
