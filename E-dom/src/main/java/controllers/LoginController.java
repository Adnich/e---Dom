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
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Osnovna validacija
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Unesite korisničko ime i lozinku.");
            return;
        }

        // Provjera korisnika u bazi
        Korisnik korisnik = korisnikDAO.nadjiPoUsernameIPassword(username, password);

        if (korisnik == null) {
            lblError.setText("Pogrešno korisničko ime ili lozinka.");
            return;
        }

        // Provjera da li je admin (po nazivu uloge u bazi, npr. 'Admin')
        if (korisnik.getUloga() == null
                || korisnik.getUloga().getNaziv() == null
                || !korisnik.getUloga().getNaziv().equalsIgnoreCase("Admin")) {

            lblError.setText("Pristup je dozvoljen samo administratoru.");
            return;
        }

        // Ako smo došli dovde -> uspješan login admina
        lblError.setText("");

        try {
            // Učitavanje admin glavnog ekrana
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/views/admin-main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            // dodajemo style.css iz /styles
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("E-Dom - Administratorski panel");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Greška pri otvaranju administratorskog ekrana.");
        }
    }

    @FXML
    private void onRegisterLinkClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("register-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 400);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("E-Dom - Registracija");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            if (lblError != null) {
                lblError.setText("Greška pri otvaranju forme za registraciju.");
            }
        }
    }
}
