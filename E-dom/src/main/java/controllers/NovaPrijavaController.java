package controllers;

import dao.PrijavaDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Prijava;
import model.StatusPrijave;
import model.Student;

import java.time.LocalDate;

public class NovaPrijavaController {

    @FXML
    private ComboBox<Student> cmbStudent;

    @FXML
    private TextField txtAkGod;

    @FXML
    private TextField txtBodovi;

    @FXML
    private ComboBox<String> cmbStatus;

    @FXML
    private TextArea txtNapomena;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    @FXML
    public void initialize() {

        // Popuni listu studenata u ComboBox
        cmbStudent.setItems(
                FXCollections.observableArrayList(studentDAO.dohvatiSveStudente())
        );

        // Kako će se student prikazati u ComboBox (ime + prezime + indeks)
        cmbStudent.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "" : s.getIme() + " " + s.getPrezime() + " (" + s.getBrojIndeksa() + ")");
            }
        });

        cmbStudent.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "" : s.getIme() + " " + s.getPrezime() + " (" + s.getBrojIndeksa() + ")");
            }
        });

        // Statusi prijave
        cmbStatus.setItems(FXCollections.observableArrayList("Na čekanju", "Odobrena", "Odbijena"));
    }

    @FXML
    private void onSaveClicked() {

        // Validacija
        if (cmbStudent.getValue() == null ||
                txtAkGod.getText().isEmpty() ||
                txtBodovi.getText().isEmpty() ||
                cmbStatus.getValue() == null) {

            showAlert("Greška", "Sva obavezna polja moraju biti popunjena.");
            return;
        }

        int akGod;
        int bodovi;

        try {
            akGod = Integer.parseInt(txtAkGod.getText());
            bodovi = Integer.parseInt(txtBodovi.getText());
        } catch (NumberFormatException e) {
            showAlert("Greška", "Akademska godina i bodovi moraju biti brojevi.");
            return;
        }

        Student s = cmbStudent.getValue();

        // StatusPrijave objekat
        StatusPrijave status = new StatusPrijave();
        status.setIdStatus(getStatusId(cmbStatus.getValue()));
        status.setNaziv(cmbStatus.getValue());

        // Kreiraj novu prijavu
        Prijava p = new Prijava();
        p.setDatumPrijava(LocalDate.now());
        p.setIdStudent(s.getIdStudent());
        p.setUkupniBodovi(bodovi);
        p.setAkademskaGodina(akGod);
        p.setNapomena(txtNapomena.getText());
        p.setStatusPrijave(status);

        prijavaDAO.unesiPrijavu(p);

        showAlert("Uspjeh", "Prijava je uspješno unesena!");

        // Zatvori prozor
        Stage stage = (Stage) txtAkGod.getScene().getWindow();
        stage.close();
    }

    private int getStatusId(String status) {
        return switch (status) {
            case "Na čekanju" -> 1;
            case "Odobrena" -> 2;
            case "Odbijena" -> 3;
            default -> 1;
        };
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
