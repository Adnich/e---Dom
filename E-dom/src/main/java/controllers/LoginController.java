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

        Korisnik k = korisnikDAO.nadjiPoUsernameIPassword(user, pass);

        if (k == null) {
            lblError.setText("Neispravni podaci za prijavu.");
            return;
        }

        if (k.getUloga() == null
                || k.getUloga().getNaziv() == null
                || !k.getUloga().getNaziv().equalsIgnoreCase("Admin")) {

            lblError.setText("Pristup je dozvoljen samo administratoru.");
            return;
        }

        lblError.setText("");

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/views/admin-main-view.fxml"));
            URL fxmlUrl = HelloApplication.class.getResource("/views/admin-main-view.fxml");
            System.out.println("ADMIN FXML URL = " + fxmlUrl);
            Parent root = loader.load();


            Scene scene = new Scene(root, 1000, 600);
            URL cssUrl = HelloApplication.class.getResource("/styles/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("⚠ style.css nije pronađen!");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("E-Dom - Administratorski panel");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Greška pri otvaranju administratorskog ekrana.");
        }

        System.out.println("Uspješna prijava: " + k);
    }

    @FXML
    private void onRegisterLinkClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/views/register-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 620);

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

    @FXML
    private void onForgotPasswordClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource("/views/izmjena-lozinke.fxml")
            );
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Reset lozinke");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
