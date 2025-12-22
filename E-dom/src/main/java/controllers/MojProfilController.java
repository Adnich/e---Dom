package controllers;

import dao.KorisnikDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Korisnik;
import service.Session;

public class MojProfilController {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtUsername;
    @FXML private TextField txtUloga;

    @FXML private PasswordField txtStaraLozinka;
    @FXML private PasswordField txtNovaLozinka;
    @FXML private PasswordField txtPotvrdaLozinke;

    @FXML private Label lblPoruka;

    private final KorisnikDAO korisnikDAO = new KorisnikDAO();
    private Korisnik korisnik;

    @FXML
    public void initialize() {
        korisnik = Session.getKorisnik();

        if (korisnik == null) {
            lblPoruka.setText("Greška: korisnik nije prijavljen.");
            return;
        }

        txtIme.setText(korisnik.getIme());
        txtPrezime.setText(korisnik.getPrezime());
        txtUsername.setText(korisnik.getUsername());
        txtUloga.setText(korisnik.getUloga().getNaziv());

        txtUsername.setDisable(true);
        txtUloga.setDisable(true);
    }

    @FXML
    private void sacuvajPromjene() {

        korisnik.setIme(txtIme.getText().trim());
        korisnik.setPrezime(txtPrezime.getText().trim());

        if (txtNovaLozinka.getText().isEmpty() &&
                txtStaraLozinka.getText().isEmpty() &&
                txtPotvrdaLozinke.getText().isEmpty()) {

            korisnikDAO.azurirajKorisnika(korisnik);
            lblPoruka.setText("Profil uspješno ažuriran.");
            return;
        }

        if (!korisnik.getPasswordHash().equals(txtStaraLozinka.getText())) {
            lblPoruka.setText("Pogrešna trenutna lozinka.");
            return;
        }

        if (!txtNovaLozinka.getText().equals(txtPotvrdaLozinke.getText())) {
            lblPoruka.setText("Nova lozinka i potvrda se ne podudaraju.");
            return;
        }

        if (txtNovaLozinka.getText().length() < 4) {
            lblPoruka.setText("Lozinka mora imati barem 4 znaka.");
            return;
        }

        korisnik.setPasswordHash(txtNovaLozinka.getText());
        korisnikDAO.azurirajKorisnika(korisnik);

        txtStaraLozinka.clear();
        txtNovaLozinka.clear();
        txtPotvrdaLozinke.clear();

        lblPoruka.setText("Profil i lozinka uspješno ažurirani.");
    }
}
