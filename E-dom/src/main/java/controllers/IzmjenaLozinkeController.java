package controllers;

import dao.KorisnikDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.EmailService;
import util.PasswordUtil;
import util.ResetTokenManager;
import model.Korisnik;
import util.TokenGenerator;

import java.sql.SQLException;

public class IzmjenaLozinkeController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblInfo;
    @FXML private TextField txtCode;
    @FXML private Button btnReset;

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

    private String generatedToken;

    @FXML
    private void onSendCodeClicked() throws SQLException {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            lblInfo.setText("Unesite korisničko ime ili email.");
            return;
        }

        Korisnik k = korisnikDAO.nadjiUsername(username);
        if (k == null) {
            lblInfo.setText("Korisnik ne postoji.");
            return;
        }

        // Generiši token
        generatedToken = TokenGenerator.generate();
        ResetTokenManager.dodajToken(k.getEmail(), generatedToken);

        // Pošalji email
        EmailService.sendResetCode(k.getEmail(), generatedToken);

        lblInfo.setText("Kod poslan na email.");
        txtCode.setDisable(false);
        txtNewPassword.setDisable(false);
        txtConfirmPassword.setDisable(false);
        btnReset.setDisable(false);
    }

    @FXML
    private void onResetClicked() throws SQLException {
        String username = txtUsername.getText().trim();
        String code = txtCode.getText().trim();
        String pass1 = txtNewPassword.getText();
        String pass2 = txtConfirmPassword.getText();

        if (!ResetTokenManager.provjeriToken(korisnikDAO.nadjiUsername(username).getEmail(), code)) {
            lblInfo.setText("Neispravan kod!");
            return;
        }

        if (!pass1.equals(pass2)) {
            lblInfo.setText("Lozinke se ne podudaraju.");
            return;
        }

        String hash = PasswordUtil.hash(pass1);
        korisnikDAO.promijeniLozinku(username, hash);
        ResetTokenManager.ukloniToken(korisnikDAO.nadjiUsername(username).getEmail());

        lblInfo.setText("Lozinka uspješno promijenjena.");
    }

}
