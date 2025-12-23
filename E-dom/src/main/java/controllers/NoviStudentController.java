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
import util.TextUtil;

import java.util.List;

public class NoviStudentController {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtIndeks;

    @FXML private ComboBox<String> cmbFakultet;
    @FXML private ComboBox<Integer> cmbGodina;

    @FXML private CheckBox chkJeLiApsolvent;
    @FXML private CheckBox chkJeLiPostdiplomac;

    @FXML private TextField txtProsjek;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefon;
    @FXML private ComboBox<SocijalniStatus> cmbSocijalniStatus;
    @FXML private TextField txtJMBG;
    @FXML private TextField txtAdresa;
    @FXML private TextField txtRoditelj;

    private final StudentDAO studentDAO = new StudentDAO();
    private final SocijalniStatusDAO socijalniStatusDAO = new SocijalniStatusDAO();

    @FXML
    public void initialize() {


        cmbFakultet.setItems(FXCollections.observableArrayList(
                "Politehnički fakultet UNZE",
                "Pravni fakultet UNZE",
                "Ekonomski fakultet UNZE",
                "Filozofski fakultet UNZE",
                "Medicinski fakultet UNZE",
                "Mašinski fakultet UNZE"
        ));


        cmbGodina.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));


        cmbSocijalniStatus.setItems(
                FXCollections.observableArrayList(socijalniStatusDAO.dohvatiSveStatuse())
        );

        cmbSocijalniStatus.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SocijalniStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNaziv());
            }
        });

        cmbSocijalniStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SocijalniStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNaziv());
            }
        });
    }


    @FXML
    public void onNextClicked() {

        // ✅ VALIDACIJA
        if (txtIme.getText().isEmpty() ||
                txtPrezime.getText().isEmpty() ||
                txtIndeks.getText().isEmpty() ||
                cmbFakultet.getValue() == null ||
                cmbGodina.getValue() == null ||
                txtProsjek.getText().isEmpty() ||
                cmbSocijalniStatus.getValue() == null ||
                txtAdresa.getText().isEmpty() ||
                txtJMBG.getText().isEmpty() ||
                txtRoditelj.getText().isEmpty()) {

            showAlert("Greška", "Sva obavezna polja moraju biti popunjena.");
            return;
        }


        if (chkJeLiApsolvent.isSelected() && chkJeLiPostdiplomac.isSelected()) {
            showAlert("Greška", "Student ne može biti i apsolvent i postdiplomac.");
            return;
        }

        int godinaStudija;
        double prosjek;

        try {
            prosjek = Double.parseDouble(txtProsjek.getText());

            if (prosjek < 6.0 || prosjek > 10.0) {
                showAlert("Greška", "Prosjek mora biti između 6.0 i 10.0.");
                return;
            }

            if (chkJeLiApsolvent.isSelected()) {
                godinaStudija = 7;
            } else if (chkJeLiPostdiplomac.isSelected()) {
                godinaStudija = 8;
            } else {
                godinaStudija = cmbGodina.getValue();
            }

        } catch (Exception e) {
            showAlert("Greška", "Neispravan unos godine ili prosjeka.");
            return;
        }


        Student s = studentDAO.findByBrojIndeksa(txtIndeks.getText());

        if (s == null) {
            s = new Student();
            s.setIme(TextUtil.formatirajIme(txtIme.getText()));
            s.setPrezime(TextUtil.formatirajIme(txtPrezime.getText()));
            s.setBrojIndeksa(txtIndeks.getText());
            s.setFakultet(cmbFakultet.getValue());
            s.setGodinaStudija(godinaStudija);
            s.setProsjek(prosjek);
            s.setEmail(txtEmail.getText());
            s.setTelefon(txtTelefon.getText());
            s.setSocijalniStatus(cmbSocijalniStatus.getValue());
            s.setJMBG(txtJMBG.getText());
            s.setAdresa(txtAdresa.getText());
            s.setImeRoditelja(txtRoditelj.getText());

            studentDAO.unesiStudent(s);
            s = studentDAO.findByBrojIndeksa(txtIndeks.getText());
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/nova-prijava.fxml"));
            Parent root = loader.load();

            NovaPrijavaController controller = loader.getController();
            controller.setStudentId(s.getIdStudent());
            controller.setGodinaStudija(godinaStudija);
            controller.setProsjek(prosjek);

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
