package controllers;

import dao.SocijalniStatusDAO;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.SocijalniStatus;
import model.Student;

public class NoviStudentController {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtIndeks;
    @FXML private TextField txtFakultet;
    @FXML private TextField txtGodina;
    @FXML private TextField txtProsjek;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefon;
    @FXML private ComboBox<SocijalniStatus> cmbSocijalniStatus;

    private final StudentDAO studentDAO = new StudentDAO();
    private final SocijalniStatusDAO socijalniStatusDAO = new SocijalniStatusDAO();

    @FXML
    public void initialize() {
        cmbSocijalniStatus.setItems(
                FXCollections.observableArrayList(socijalniStatusDAO.dohvatiSveStatuse())
        );
    }

    @FXML
    public void onNextClicked() {

        if (txtIme.getText().isEmpty() ||
                txtPrezime.getText().isEmpty() ||
                txtIndeks.getText().isEmpty() ||
                txtFakultet.getText().isEmpty() ||
                txtGodina.getText().isEmpty() ||
                txtProsjek.getText().isEmpty() ||
                cmbSocijalniStatus.getValue() == null) {

            showAlert("Greška", "Sva obavezna polja moraju biti popunjena.");
            return;
        }

        int godinaStudija;
        double prosjek;

        try {
            godinaStudija = Integer.parseInt(txtGodina.getText());
            prosjek = Double.parseDouble(txtProsjek.getText());
        } catch (Exception e) {
            showAlert("Greška", "Godina i prosjek moraju biti numerički.");
            return;
        }

        // Provjera postoji li student
        Student postojeci = studentDAO.findByBrojIndeksa(txtIndeks.getText());
        Student s;

        if (postojeci == null) {

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

            // PREPORUKA: bolje dohvati ID jedino upisanog studenta
            s = studentDAO.findByBrojIndeksa(txtIndeks.getText());
        } else {
            s = postojeci;
        }

        int studentId = s.getIdStudent();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/nova-prijava.fxml"));
            Parent root = loader.load();

            NovaPrijavaController controller = loader.getController();
            controller.setStudentId(studentId);

            Stage stage = (Stage) txtIme.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Greška", "Ne mogu učitati formu za prijavu.");
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
