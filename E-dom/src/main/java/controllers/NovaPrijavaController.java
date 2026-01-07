package controllers;

import controllers.DodajDokumenteControllers.BraniociDokumentiController;
import dao.PrijavaDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import service.BraniociRezultat;
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
    @FXML private VBox vboxBranioci;

    private BraniociDokumentiController braniociController;
    private int studentId;
    private double prosjek;
    private int godinaStudija;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    @FXML
    public void initialize() {
        txtAkGod.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCssIfMissing(newScene);
                ucitajBraniociFormu();
            }
        });

        if (txtAkGod.getScene() != null) {
            applyCssIfMissing(txtAkGod.getScene());
            ucitajBraniociFormu();
        }
    }


    private void applyCssIfMissing(Scene scene) {
        var cssUrl = getClass().getResource("/styles/nova-prijava-style.css");
        if (cssUrl != null) {
            String css = cssUrl.toExternalForm();
            if (!scene.getStylesheets().contains(css)) {
                scene.getStylesheets().add(css);
            }
        }
    }

    // ===============================
    // FULLSCREEN / MAXIMIZE FIX
    // ===============================
    private void forceMaximize(Stage stage) {
        Platform.runLater(() -> {
            stage.setResizable(true);
            stage.setMaximized(true);

            // ✅ Windows fix (ako maximize ne radi)
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        });
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

    // ===============================
    // SAVE
    // ===============================
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

        if (akGod < LocalDate.now().getYear()) {
            showAlert("Greška", "Akademska godina ne može biti manja od tekuće godine.");
            return;
        }

        Prijava p = new Prijava();
        p.setIdStudent(studentId);
        p.setDatumPrijava(LocalDate.now());
        p.setAkademskaGodina(akGod);
        p.setNapomena(txtNapomena.getText());
        p.setUkupniBodovi(0);

        StatusPrijave status = new StatusPrijave(1, "Na čekanju");
        p.setStatusPrijave(status);

        prijavaDAO.unesiPrijavu(p);

        int prijavaId = p.getIdPrijava();
        System.out.println("Nova prijava unesena sa ID: " + prijavaId);


        // dodatni bodovi
        int dodatniBodovi = 0;
        if (chkIzbjeglica.isSelected()) dodatniBodovi += 3;
        if (chkBratSestra.isSelected()) dodatniBodovi += 2;

        if (dodatniBodovi > 0) {
            prijavaDAO.dodajBodoveNaPrijavu(prijavaId, dodatniBodovi);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dodaj-dokumente.fxml"));
            Parent root = loader.load();

            DodajDokumenteController controller = loader.getController();
            controller.setPrijavaId(prijavaId);
            controller.setUdaljenost(udaljenost);
            controller.setProsjek(prosjek);
            controller.setGodinaStudija(godinaStudija);
            controller.setIzbjeglica(chkIzbjeglica.isSelected());
            controller.setBratSestra(chkBratSestra.isSelected());

            double bodoviBranioci = 0;
            // Prosljeđivanje rezultata branilaca (bodovi + naziv)
            BraniociRezultat rezultat = null;
            if (braniociController != null) {
                rezultat = braniociController.izracunajBodove();
            }

            controller.setBraniociRezultat(rezultat);
            controller.setClanovi(clanovi);

            Stage stage = (Stage) txtAkGod.getScene().getWindow();

            // ✅ Scene + CSS
            Scene novaScene = new Scene(root);
            applyCssIfMissing(novaScene); // ako želiš da bude isti css

            stage.setScene(novaScene);

            // ✅ VRATI FULLSCREEN
            forceMaximize(stage);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za dokumente.");
        }
    }

    // ===============================
    // ALERT
    // ===============================
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ===============================
    // BRANIOCI FORMA
    // ===============================
    private void ucitajBraniociFormu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DodajDokumenteSections/branioci-forma.fxml"));

            Node node = loader.load();
            braniociController = loader.getController();

            vboxBranioci.getChildren().clear();
            vboxBranioci.getChildren().add(node);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za branioce.");
        }
    }

// ===============================
// BACK (Nazad)
// ===============================
@FXML
private void onBackClicked() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/novi-student.fxml"));
        Parent root = loader.load();

        NoviStudentController controller = loader.getController();

        // ✅ ako želiš vratiti unesene vrijednosti (opcionalno)
        // controller.setStudentId(studentId);
        // controller.setProsjek(prosjek);
        // controller.setGodinaStudija(godinaStudija);

        Stage stage = (Stage) txtAkGod.getScene().getWindow();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // ✅ vrati fullscreen
        forceMaximize(stage);

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Greška", "Ne mogu učitati formu za studenta.");
    }
}
}