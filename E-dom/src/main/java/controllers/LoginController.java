package controllers;

import dao.KorisnikDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Korisnik;
import org.example.edom.HelloApplication;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private final KorisnikDAO korisnikDAO = new KorisnikDAO();

    @FXML
    private void onLoginClicked(ActionEvent event) {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Unesite korisničko ime i lozinku.");
            return;
        }

        // ako u bazi čuvate hash, ovdje trebaš uraditi hash(pass)
        Korisnik k = korisnikDAO.nadjiPoUsernameIPassword(user, pass);

        if (k == null) {
            lblError.setText("Neispravni podaci za prijavu.");
            return;
        }

        // TODO: ovdje ćeš kasnije otvoriti admin / student prozor,
        // npr. na osnovu k.getUloga().getNaziv()
        lblError.setText(""); // očisti poruku
        System.out.println("Uspješna prijava: " + k);
    }

    @FXML
    private void onRegisterLinkClicked(ActionEvent event) {
        try {
            // učitavamo register-view.fxml iz istog paketa kao i login-view.fxml
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("register-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 600, 450);

            // >>> DODAJEMO CSS (ISTO KAO U HelloApplication) <<<
            URL cssUrl = HelloApplication.class.getResource("/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠ style.css nije pronađen!");
            }

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("E-Dom - Registracija");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Greška pri otvaranju registracije.");
        }
    }
}
