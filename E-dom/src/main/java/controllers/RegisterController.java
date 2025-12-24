package controllers;

import dao.KorisnikDAO;
import dao.UlogaDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Korisnik;
import model.Uloga;
import org.example.edom.HelloApplication;
import util.PasswordUtil;
import util.TextUtil;

import java.io.IOException;
import java.sql.SQLException;
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
        List<Uloga> uloge = ulogaDAO.dohvatiSveUloge();
        cmbUloga.setItems(FXCollections.observableArrayList(uloge));
        cmbUloga.setPromptText("Odaberite ulogu");

        cmbUloga.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Uloga uloga, boolean empty) {
                super.updateItem(uloga, empty);
                setText((empty || uloga == null) ? "" : uloga.getNaziv());
            }
        });

        cmbUloga.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Uloga uloga, boolean empty) {
                super.updateItem(uloga, empty);
                setText((empty || uloga == null) ? "" : uloga.getNaziv());
            }
        });
    }


    @FXML
    private void onRegisterClicked(ActionEvent event) throws SQLException {
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

        if(korisnikDAO.nadjiUsername(username) != null) {
            lblError.setText("Korisničko ime je zauzeto.");
            return;
        }
        if(pass1.length()<8){
            lblError.setText("Lozinka mora imati najmanje 8 karaktera.");
            return;
        }
        if(!pass1.matches(".*[A-Z].*")){
            lblError.setText("Lozinka mora sadržavati barem jedno veliko slovo.");
            return;
        }
        if(!pass1.matches(".*[a-z].*")){
            lblError.setText("Lozinka mora sadržavati barem jedno malo slovo.");
            return;
        }
        if(!pass1.matches(".*\\d.*")){
            lblError.setText("Lozinka mora sadržavati barem jednu cifru.");
            return;
        }
        if(!pass1.matches(".*[!@#$%^&*()].*")){
            lblError.setText("Lozinka mora sadržavati barem jedan specijalni karakter (!@#$%^&*()).");
            return;
        }
        ime = TextUtil.formatirajIme(ime);
        prezime = TextUtil.formatirajIme(prezime);
        // ovdje za projekat koristimo lozinku direktno kao password_hash
        Korisnik k = new Korisnik();
        k.setIme(ime);
        k.setPrezime(prezime);
        k.setUsername(username);
        k.setPasswordHash(PasswordUtil.hash(pass1));
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
            FXMLLoader loader =
                    new FXMLLoader(HelloApplication.class.getResource("/views/login-view.fxml"));

            Scene scene = new Scene(loader.load(), 1000, 600);

            // SIGURNO učitavanje CSS-a
            var cssUrl = HelloApplication.class.getResource("/styles/login-style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("E-Dom - Prijava");

            stage.setScene(scene);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
