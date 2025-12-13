package controllers;

import dao.PrijavaDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Prijava;
import model.StatusPrijave;

import java.time.LocalDate;

public class NovaPrijavaController {

    @FXML private TextField txtAkGod;
    @FXML private TextArea txtNapomena;
    @FXML private TextField txtClanovi;

    private int studentId;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    public void setStudentId(int id) {
        this.studentId = id;
    }

    @FXML
    private void onSaveClicked() {

        if (txtAkGod.getText().isEmpty()) {
            showAlert("Greška", "Akademska godina je obavezna.");
            return;
        }

        int akGod;

        try {
            akGod = Integer.parseInt(txtAkGod.getText());
        } catch (Exception e) {
            showAlert("Greška", "Akademska godina mora biti broj.");
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

        showAlert("Uspjeh", "Prijava uspješno unešena!");

        Prijava prijava = prijavaDAO.dohvatiSvePrijave().getLast();
        int prijavaId = prijava.getIdPrijava();
        int clanovi = Integer.parseInt(txtClanovi.getText());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dodaj-dokumente.fxml"));
            Parent root = loader.load();

            DodajDokumenteController controller = loader.getController();
            controller.setPrijavaId(prijavaId);
            controller.setClanovi(clanovi);

            Stage stage = (Stage) txtAkGod.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za dokumente.");
        }

        Stage stage = (Stage) txtAkGod.getScene().getWindow();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
