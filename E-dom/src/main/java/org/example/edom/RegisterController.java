package org.example.edom;

import dao.KorisnikDAO;
import dao.UlogaDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Korisnik;
import model.Uloga;

import java.io.IOException;
import java.util.List;

public class RegisterController {

    @FXML
    private TextField txtIme;

    @FXML
    private TextField txtPrezime;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtPassword2;

    @FXML
    private ComboBox<Uloga> cmbUloga;

    @FXML
    private Label lblError;

    private final UlogaDAO ulogaDAO = new UlogaDAO();
    private final KorisnikDAO korisnikDAO = new KorisnikDAO();

    @FXML
    public void initialize() {
        // Učitavanje uloga iz baze pri otvaranju forme
        List<Uloga> uloge = ulogaDAO.dohvatiSveUloge();
        cmbUloga.setItems(FXCollections.observableArrayList(uloge));
        cmbUloga.setPromptText("Odaberite ulogu");
    }

    @FXML
    private void onRegisterClicked(ActionEvent event) {
        String ime = txtIme.getText().trim();
        String prezime = txtPrezime.getText().trim();
        String username = txtUsername.getText().trim();
        String pass1 = txtPassword.getText();
        String pass2 = txtPassword2.getText();
        Uloga uloga = cmbUloga.getValue();

        // osnovne provjere
        if (ime.isEmpty() || prezime.isEmpty() || username.isEmpty()
                || pass1.isEmpty() || pass2.isEmpty() || uloga == null) {
            lblError.setText("Popunite sva polja i odaberite ulogu.");
            return;
        }

        if (!pass1.equals(pass2)) {
            lblError.setText("Lozinke se ne podudaraju.");
            return;
        }

        // ovdje za projekat koristimo lozinku direktno kao password_hash
        Korisnik k = new Korisnik();
        k.setIme(ime);
        k.setPrezime(prezime);
        k.setUsername(username);
        k.setPasswordHash(pass1);
        k.setUloga(uloga);

        try {
            korisnikDAO.unesiKorisnika(k);

            // mala potvrda korisniku
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registracija");
            alert.setHeaderText(null);
            alert.setContentText("Korisnik je uspješno registrovan.");
            alert.showAndWait();

            // nakon uspješne registracije, vratimo se na login
            otvoriLoginEkran(event);

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Greška pri registraciji.");
        }
    }

    @FXML
    private void onBackToLoginClicked(ActionEvent event) {
        otvoriLoginEkran(event);
    }

    private void otvoriLoginEkran(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(
                    HelloApplication.class.getResource("styles/style.css").toExternalForm()
            );

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("E-Dom - Prijava");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
