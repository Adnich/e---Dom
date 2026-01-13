package controllers;

import dao.KorisnikDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Korisnik;
import service.Session;
import util.TextUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;

public class MojProfilController {

    @FXML private TextField txtIme;
    @FXML private TextField txtPrezime;
    @FXML private TextField txtUsername;
    @FXML private TextField txtUloga;

    @FXML private PasswordField txtStaraLozinka;
    @FXML private PasswordField txtNovaLozinka;
    @FXML private PasswordField txtPotvrdaLozinke;

    @FXML private Label lblPoruka;
    @FXML private Label lblZadnjaPrijava;

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
        lblZadnjaPrijava.setText(
                formatZadnjaPrijava(korisnik.getZadnjaPrijava())
        );
    }

    @FXML
    private void sacuvajPromjene() {

        korisnik.setIme(TextUtil.formatirajIme(txtIme.getText().trim()));
        korisnik.setPrezime(TextUtil.formatirajIme(txtPrezime.getText().trim()));

        if (txtNovaLozinka.getText().isEmpty() &&
                txtStaraLozinka.getText().isEmpty() &&
                txtPotvrdaLozinke.getText().isEmpty()) {

            korisnikDAO.azurirajKorisnika(korisnik);
            lblPoruka.setText("Profil uspješno ažuriran.");
            return;
        }

        Korisnik provjera = korisnikDAO.nadjiPoUsernameIPassword(
                korisnik.getUsername(),
                txtStaraLozinka.getText()
        );

        if (provjera == null) {
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

        if(txtStaraLozinka.getText().equals(txtNovaLozinka.getText())){
            lblPoruka.setText("Nova lozinka mora biti različita od stare lozinke.");
            return;
        }


        if (!txtNovaLozinka.getText().matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$")) {
            lblPoruka.setText("Lozinka mora sadržavati barem jedno veliko slovo, jedno malo slovo, jedan broj i jedan specijalni znak.");
            return;
        }

        korisnik.setPasswordHash(txtNovaLozinka.getText());
        korisnikDAO.azurirajProfil(korisnik);

        txtStaraLozinka.clear();
        txtNovaLozinka.clear();
        txtPotvrdaLozinke.clear();

        lblPoruka.setText("Profil i lozinka uspješno ažurirani.");
    }

    private String formatZadnjaPrijava(Timestamp ts) {
        if (ts == null) {
            return "Prva prijava";
        }

        LocalDateTime last = ts.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long minutes = java.time.Duration.between(last, now).toMinutes();
        long hours   = java.time.Duration.between(last, now).toHours();
        long days    = java.time.Duration.between(last, now).toDays();

        if (minutes < 1) {
            return "Upravo sada";
        } else if (minutes < 60) {
            return "Prije " + minutes + " min";
        } else if (hours < 24) {
            return "Prije " + hours + " h";
        } else if (days == 1) {
            return "Jučer u " + last.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else if (days < 7) {
            return "Prije " + days + " dana";
        } else {
            return last.format(DateTimeFormatter.ofPattern("dd.MM.yyyy 'u' HH:mm"));
        }
    }
}
