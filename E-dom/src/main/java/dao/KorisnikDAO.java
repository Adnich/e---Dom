package dao;

import model.Korisnik;
import model.Uloga;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KorisnikDAO {

    // UNOS NOVOG KORISNIKA
    public void unesiKorisnika(Korisnik k) {
        String sql = "INSERT INTO korisnik (ime, prezime, username, password_hash, ulogaid_uloga) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, k.getIme());
            stmt.setString(2, k.getPrezime());
            stmt.setString(3, k.getUsername());
            stmt.setString(4, k.getPasswordHash());
            stmt.setInt(5, k.getUloga().getIdUloga()); // FK na uloga

            stmt.executeUpdate();
            System.out.println("Korisnik je uspješno dodan!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // DOHVATANJE SVIH KORISNIKA
    public List<Korisnik> dohvatiSveKorisnike() {
        List<Korisnik> lista = new ArrayList<>();

        String sql = "SELECT k.*, u.id_uloga, u.naziv AS naziv_uloge " +
                "FROM korisnik k " +
                "JOIN uloga u ON k.ulogaid_uloga = u.id_uloga";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Korisnik k = new Korisnik();

                k.setIdKorisnik(rs.getInt("id_korisnik"));
                k.setIme(rs.getString("ime"));
                k.setPrezime(rs.getString("prezime"));
                k.setUsername(rs.getString("username"));
                k.setPasswordHash(rs.getString("password_hash"));

                // ULOGA objekat
                Uloga u = new Uloga(
                        rs.getInt("id_uloga"),
                        rs.getString("naziv_uloge")
                );

                k.setUloga(u);
                lista.add(k);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return lista;
    }

    // AŽURIRANJE KORISNIKA
    public void azurirajKorisnika(Korisnik k) {
        String sql = "UPDATE korisnik SET ime=?, prezime=?, username=?, password_hash=?, " +
                "ulogaid_uloga=? WHERE id_korisnik=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, k.getIme());
            stmt.setString(2, k.getPrezime());
            stmt.setString(3, k.getUsername());
            stmt.setString(4, k.getPasswordHash());
            stmt.setInt(5, k.getUloga().getIdUloga());
            stmt.setInt(6, k.getIdKorisnik());

            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("Korisnik ažuriran!");
            else
                System.out.println("Korisnik sa ID " + k.getIdKorisnik() + " ne postoji!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // BRISANJE KORISNIKA ------------------------------------------------------
    public void obrisiKorisnika(int id) {
        String sql = "DELETE FROM korisnik WHERE id_korisnik=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            if (rows > 0)
                System.out.println("Korisnik obrisan!");
            else
                System.out.println("Korisnik sa ID " + id + " ne postoji!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // LOGIN METODA
    public Korisnik nadjiPoUsernameIPassword(String username, String passwordHash) {
        String sql = "SELECT k.*, u.id_uloga, u.naziv AS naziv_uloge " +
                "FROM korisnik k " +
                "JOIN uloga u ON k.ulogaid_uloga = u.id_uloga " +
                "WHERE k.username = ? AND k.password_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    Korisnik k = new Korisnik();
                    k.setIdKorisnik(rs.getInt("id_korisnik"));
                    k.setIme(rs.getString("ime"));
                    k.setPrezime(rs.getString("prezime"));
                    k.setUsername(rs.getString("username"));
                    k.setPasswordHash(rs.getString("password_hash"));

                    Uloga u = new Uloga(
                            rs.getInt("id_uloga"),
                            rs.getString("naziv_uloge")
                    );

                    k.setUloga(u);

                    return k;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null; // ako nije pronađen
    }

    public Korisnik nadjiUsername(String username) throws SQLException {
        String sql = "SELECT k.*, u.id_uloga, u.naziv AS naziv_uloge " +
                "FROM korisnik k " +
                "JOIN uloga u ON k.ulogaid_uloga = u.id_uloga " +
                "WHERE k.username = ?";

        try(Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try(ResultSet rd = stmt.executeQuery()) {
                if(rd.next()) {
                    Korisnik k = new Korisnik();
                    k.setIdKorisnik(rd.getInt("id_korisnik"));
                    k.setIme(rd.getString("ime"));
                    k.setPrezime(rd.getString("prezime"));
                    k.setUsername(rd.getString("username"));
                    k.setPasswordHash(rd.getString("password_hash"));

                    Uloga u = new Uloga(
                            rd.getInt("id_uloga"),
                            rd.getString("naziv_uloge")
                    );

                    k.setUloga(u);

                    return k;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    //metoda za reset lozinke
    public boolean promijeniLozinku(String username, String newPasswordHash) {
        String sql = "UPDATE korisnik SET password_hash = ? WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPasswordHash);
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
