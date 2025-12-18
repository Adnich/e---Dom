package controllers;

import controllers.DodajDokumenteControllers.BraniociDokumentiController;
import dao.PrijavaDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Prijava;
import model.StatusPrijave;

import java.time.LocalDate;

public class NovaPrijavaController {

    @FXML private TextField txtAkGod;
    @FXML private TextArea txtNapomena;
    @FXML private TextField txtClanovi;
    @FXML private TextField txtUdaljenost;
    @FXML private CheckBox chkIzbjeglica;
    @FXML private CheckBox chkBratSestra;

    private BraniociDokumentiController braniociController;

    @FXML private VBox vboxBranioci; // dodaj u FXML NovaPrijava


    private int studentId;
    private double prosjek;
    private int godinaStudija;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    // ✅ DODANO: automatsko vezanje CSS-a kad se view prikaže
    @FXML
    public void initialize() {

        txtAkGod.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCssIfMissing(newScene);
                ucitajBraniociFormu(); // ✅ OVDJE
            }
        });

        if (txtAkGod.getScene() != null) {
            applyCssIfMissing(txtAkGod.getScene());
            ucitajBraniociFormu(); // ✅ I OVDJE
        }
    }


    private void applyCssIfMissing(Scene scene) {
        var cssUrl = getClass().getResource("/styles/nova-prijava-style.css"); // prilagodi put ako ti je /css/ umjesto /styles/
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        } else {
            System.out.println("⚠ nova-prijava-style.css nije pronađen! Provjeri put: /styles/nova-prijava-style.css");
        }
    }

    public void setStudentId(int id) {
        this.studentId = id;
    }

    public void setGodinaStudija(int godinaStudija) {
        this.godinaStudija = godinaStudija;
    }

    public void setProsjek(double prosjek) {
        this.prosjek = prosjek;
    }

    @FXML
    private void onSaveClicked() {

        if (txtAkGod.getText().isEmpty()
                || txtClanovi.getText().isEmpty()
                || txtUdaljenost.getText().isEmpty()) {
            showAlert("Greška", "Sva polja su obavezna.");
            return;
        }

        int akGod, clanovi;
        double udaljenost;

        try {
            akGod = Integer.parseInt(txtAkGod.getText());
            clanovi = Integer.parseInt(txtClanovi.getText());
            udaljenost = Double.parseDouble(txtUdaljenost.getText());
        } catch (Exception e) {
            showAlert("Greška", "Neispravan unos brojeva.");
            return;
        }

        // 1️⃣ KREIRANJE PRIJAVE
        Prijava p = new Prijava();
        p.setIdStudent(studentId);
        p.setDatumPrijava(LocalDate.now());
        p.setAkademskaGodina(akGod);
        p.setNapomena(txtNapomena.getText());
        p.setUkupniBodovi(0);

        StatusPrijave status = new StatusPrijave(1, "Na čekanju");
        p.setStatusPrijave(status);

        prijavaDAO.unesiPrijavu(p);

        Prijava prijava = prijavaDAO.dohvatiSvePrijave().getLast();
        int prijavaId = prijava.getIdPrijava();

        int dodatniBodovi = 0;

        if (chkIzbjeglica.isSelected()) {
            dodatniBodovi += 3;
        }

        if (chkBratSestra.isSelected()) {
            dodatniBodovi += 2;
        }

        if (dodatniBodovi > 0) {
            prijavaDAO.dodajBodoveNaPrijavu(prijavaId, dodatniBodovi);
        }

        // 4️⃣ PRELAZ NA DOKUMENTE
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/dodaj-dokumente.fxml")
            );
            Parent root = loader.load();

            DodajDokumenteController controller = loader.getController();
            controller.setPrijavaId(prijavaId);
            controller.setClanovi(clanovi);
            controller.setUdaljenost(udaljenost);
            controller.setProsjek(prosjek);
            controller.setGodinaStudija(godinaStudija);

            Stage stage = (Stage) txtAkGod.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za dokumente.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void ucitajBraniociFormu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/DodajDokumenteSections/branioci-forma.fxml")
            );

            Node node = loader.load();
            braniociController = loader.getController();

            vboxBranioci.getChildren().clear();
            vboxBranioci.getChildren().add(node);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za branioce.");
        }
    }

}
