package controllers;

import dao.PrijavaDAO;
import dao.SocijalniStatusDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Prijava;
import model.SocijalniStatus;
import model.StatusPrijave;
import model.Student;

import java.time.LocalDate;

public class NovaPrijavaController {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtIndeks;
    @FXML private TextField txtFakultet;
    @FXML private TextField txtGodina;
    @FXML private TextField txtProsjek;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefon;

    @FXML private ComboBox<SocijalniStatus> cmbSocijalniStatus;

    @FXML private TextField txtAkGod;
    @FXML private TextArea txtNapomena;

    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private final SocijalniStatusDAO socijalniStatusDAO = new SocijalniStatusDAO();

    @FXML
    public void initialize() {
        // popuna ComboBox-a socijalnim statusima
        cmbSocijalniStatus.setItems(
                FXCollections.observableArrayList(socijalniStatusDAO.dohvatiSveStatuse())
        );
    }

    @FXML
    private void onSaveClicked() {

        // Validacija osnovnih polja
        if (txtIme.getText().isEmpty() ||
                txtPrezime.getText().isEmpty() ||
                txtIndeks.getText().isEmpty() ||
                txtFakultet.getText().isEmpty() ||
                txtGodina.getText().isEmpty() ||
                txtProsjek.getText().isEmpty() ||
                cmbSocijalniStatus.getValue() == null ||
                txtAkGod.getText().isEmpty()) {

            showAlert("Greška", "Sva obavezna polja moraju biti popunjena.");
            return;
        }

        int godinaStudija;
        double prosjek;
        int akGod;

        try {
            godinaStudija = Integer.parseInt(txtGodina.getText());
            prosjek = Double.parseDouble(txtProsjek.getText());
            akGod = Integer.parseInt(txtAkGod.getText());
        } catch (NumberFormatException e) {
            showAlert("Greška", "Godina studija, prosjek i akademska godina moraju biti numerički.");
            return;
        }

        // 1) Provjera da li student postoji
        Student postojeci = studentDAO.findByBrojIndeksa(txtIndeks.getText());

        Student s;

        if (postojeci == null) {
            // 2) Kreiranje novog studenta
            s = new Student();
            s.setIme(txtIme.getText());
            s.setPrezime(txtPrezime.getText());
            s.setBrojIndeksa(txtIndeks.getText());
            s.setFakultet(txtFakultet.getText());
            s.setGodinaStudija(godinaStudija);
            s.setProsjek(prosjek);
            s.setEmail(txtEmail.getText());
            s.setTelefon(txtTelefon.getText());
            s.setSocijalniStatus(cmbSocijalniStatus.getValue());

            studentDAO.unesiStudent(s);
        } else {
            s = postojeci;
        }

        // 3) Kreiramo prijavu sa bodovi = 0
        Prijava p = new Prijava();
        p.setIdStudent(s.getIdStudent());
        p.setDatumPrijava(LocalDate.now());
        p.setAkademskaGodina(akGod);
        p.setNapomena(txtNapomena.getText());
        p.setUkupniBodovi(0);   // DEFAULT

        // StatusPrijave = 1 → "Na čekanju"
        StatusPrijave status = new StatusPrijave(1, "Na čekanju");
        p.setStatusPrijave(status);

        prijavaDAO.unesiPrijavu(p);

        showAlert("Uspjeh", "Nova prijava uspješno unesena!");

        // zatvori prozor
        Stage stage = (Stage) txtIme.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
