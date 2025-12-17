package controllers;

import dao.KorisnikDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class IzmjenaLozinkeController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblInfo;

    private final KorisnikDAO korisnikDAO = new KorisnikDAO();

    // ✅ DODANO – fiksiranje veličine prozora
    @FXML
    public void initialize() {
        txtUsername.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();

                stage.setWidth(400);
                stage.setHeight(300);
                stage.setResizable(false);
                stage.centerOnScreen();
            }
        });
    }

    @FXML
    private void onResetClicked() {

        String username = txtUsername.getText().trim();
        String pass1 = txtNewPassword.getText();
        String pass2 = txtConfirmPassword.getText();

        if (username.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            lblInfo.setText("Sva polja su obavezna.");
            return;
        }

        if (!pass1.equals(pass2)) {
            lblInfo.setText("Lozinke se ne podudaraju.");
            return;
        }

        boolean uspjeh = korisnikDAO.promijeniLozinku(username, pass1);

        if (!uspjeh) {
            lblInfo.setText("Korisnik ne postoji.");
            return;
        }

        lblInfo.setText("Lozinka uspješno promijenjena.");

        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }
}
