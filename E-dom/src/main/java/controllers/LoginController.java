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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Korisnik;
import org.example.edom.HelloApplication;
import service.Session;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPasswordVisible;

    @FXML
    private javafx.scene.control.Button btnTogglePassword;

    @FXML
    private ImageView imgEye;

    @FXML
    private Label lblError;

    private final KorisnikDAO korisnikDAO = new KorisnikDAO();

    private boolean passwordShown = false;

    @FXML
    private void initialize() {
        // da oba polja uvijek imaju isti tekst
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        // start: sakriveno
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);

        txtPassword.setVisible(true);
        txtPassword.setManaged(true);

        setEyeIcon(false);
    }

    @FXML
    private void onTogglePasswordVisibility() {
        passwordShown = !passwordShown;

        // toggle vidljivost polja
        txtPasswordVisible.setVisible(passwordShown);
        txtPasswordVisible.setManaged(passwordShown);

        txtPassword.setVisible(!passwordShown);
        txtPassword.setManaged(!passwordShown);

        // zadrži fokus i caret na kraju
        if (passwordShown) {
            txtPasswordVisible.requestFocus();
            txtPasswordVisible.positionCaret(txtPasswordVisible.getText().length());
        } else {
            txtPassword.requestFocus();
            txtPassword.positionCaret(txtPassword.getText().length());
        }

        setEyeIcon(passwordShown);
    }

    private void setEyeIcon(boolean shown) {
        String path = shown ? "/images/eye-off.png" : "/images/eye.png";
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            imgEye.setImage(img);
        } catch (Exception e) {
            // ako slika nije nađena, bar da app ne crasha
            System.out.println("⚠ Ne mogu učitati ikonu: " + path);
        }
    }

    /**
     * Helper metoda koja otvara novu scenu preko cijelog ekrana (maximized)
     */
    private void openScene(Stage stage, Parent root, String title) {
        Scene scene = new Scene(root);

        URL cssUrl = HelloApplication.class.getResource("/styles/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.out.println("⚠ style.css nije pronađen!");
        }

        stage.setTitle(title);
        stage.setScene(scene);

        // ✅ obavezno da je resizable
        stage.setResizable(true);

        // ✅ prvo pokaži prozor
        stage.show();

        // ✅ zatim tek maximize (najsigurnije)
        Platform.runLater(() -> {
            stage.setMaximized(true);

            // BONUS: ako maximize i dalje ne radi na nekim Windows konfiguracijama
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        });
    }


    @FXML
    private void onLoginClicked(ActionEvent event) throws SQLException {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim(); // dovoljno je ovo, jer je bind na oba

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Unesite korisničko ime i lozinku.");
            return;
        }

        Korisnik k = korisnikDAO.nadjiUsername(user);
        if (k == null || !k.ProvjeriPassword(pass)) {
            lblError.setText("Neispravni podaci za prijavu.");
            return;
        }

        if (k.getUloga() == null
                || k.getUloga().getNaziv() == null
                || !k.getUloga().getNaziv().equalsIgnoreCase("Admin")) {

            lblError.setText("Pristup je dozvoljen samo administratoru.");
            return;
        }

        Session.setKorisnik(k);
        lblError.setText("");

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("/views/admin-main-view.fxml"));
            URL fxmlUrl = HelloApplication.class.getResource("/views/admin-main-view.fxml");
            System.out.println("ADMIN FXML URL = " + fxmlUrl);

            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // ✅ otvori preko cijelog ekrana
            openScene(stage, root, "E-Dom - Administratorski panel");

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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // ✅ otvori preko cijelog ekrana
            openScene(stage, root, "E-Dom - Registracija");

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

            Parent root = loader.load();

            Stage stage = new Stage();

            // ✅ otvori preko cijelog ekrana i novi prozor
            openScene(stage, root, "Reset lozinke");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
