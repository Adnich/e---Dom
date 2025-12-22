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
        cmbSocijalniStatus.setItems(
                FXCollections.observableArrayList(socijalniStatusDAO.dohvatiSveStatuse())
        );

        cmbSocijalniStatus.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SocijalniStatus sStatus, boolean empty){
                super.updateItem(sStatus, empty);
                setText((empty || sStatus == null) ? "" : sStatus.getNaziv());
            }
        });

        cmbSocijalniStatus.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SocijalniStatus sStatus, boolean empty){
                super.updateItem(sStatus, empty);
                setText((empty || sStatus == null) ? "" : sStatus.getNaziv());
            }
        });

        cmbFakultet.setItems(FXCollections.observableArrayList(
                "Politehnički fakultet UNZE",
                "Pravni fakultet UNZE",
                "Mašinski fakultet UNZE",
                "Ekonomski fakultet UNZE",
                "Medicinski fakultet UNZE",
                "Islamsko pedagoški fakultet",
                "Fakultet za metalurgiju i materijale",
                "Fakultet zdravstvenih studija"
        ));

        cmbFakultet.setEditable(false);

        cmbGodina.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6
        ));
        cmbGodina.setEditable(false);

        loadStyles();
    }

    private void loadStyles() {
        try {
            // Sačekaj da se scene učita, pa dodaj stylesheet
            txtIme.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    String cssPath = getClass().getResource("/styles/novi-student-style.css").toExternalForm();
                    if (!newScene.getStylesheets().contains(cssPath)) {
                        newScene.getStylesheets().add(cssPath);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("CSS file not found: " + e.getMessage());
        }
    }

    @FXML
    public void onNextClicked() {
        String email = txtEmail.getText().trim();

        if (txtIme.getText().isEmpty() ||
                txtPrezime.getText().isEmpty() ||
                txtIndeks.getText().isEmpty() ||
                cmbFakultet.getValue() == null ||
                txtProsjek.getText().isEmpty() ||
                cmbSocijalniStatus.getValue() == null ||
                txtAdresa.getText().isEmpty() ||
                txtJMBG.getText().isEmpty() ||
                txtRoditelj.getText().isEmpty()) {

            showAlert("Greška", "Sva obavezna polja moraju biti popunjena.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showAlert("Greška", "Unesite ispravnu email adresu (mora sadržavati @ i .).");
            return;
        }

        //NE MOGU OBA BITI OZNAČENA
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

        } catch (NumberFormatException e) {
            showAlert("Greška", "Prosjek mora biti broj (npr. 8.45).");
            return;
        }

        Student postojeci = studentDAO.findByBrojIndeksa(txtIndeks.getText());
        Student s;

        if (postojeci == null) {

            s = new Student();
            s.setIme(txtIme.getText());
            s.setPrezime(txtPrezime.getText());
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
        } else {
            s = postojeci;
        }

        int studentId = s.getIdStudent();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/nova-prijava.fxml"));
            Parent root = loader.load();

            NovaPrijavaController controller = loader.getController();
            controller.setStudentId(studentId);
            controller.setGodinaStudija(godinaStudija);
            controller.setProsjek(prosjek);

            Stage stage = (Stage) txtIme.getScene().getWindow();
            Scene scene = new Scene(root);

            try {
                String cssPath = getClass().getResource("/style/novi-student-style.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("CSS not loaded for nova-prijava scene: " + e.getMessage());
            }

            stage.setScene(scene);

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