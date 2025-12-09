package dao;

import model.SocijalniStatus;
import model.Student;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // UNOS NOVOG STUDENTA ----------------------------------------------------
    public void unesiStudent(Student s) {
        String sql = "INSERT INTO student " +
                "(ime, prezime, broj_indeksa, fakultet, godina_studija, prosjek, " +
                "email, telefon, socijalni_status_id_status) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getIme());
            stmt.setString(2, s.getPrezime());
            stmt.setString(3, s.getBrojIndeksa());
            stmt.setString(4, s.getFakultet());
            stmt.setInt(5, s.getGodinaStudija());
            stmt.setDouble(6, s.getProsjek());
            stmt.setString(7, s.getEmail());
            stmt.setString(8, s.getTelefon());
            stmt.setInt(9, s.getSocijalniStatus().getIdStatus());

            stmt.executeUpdate();
            System.out.println("Student uspješno unesen!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // DOHVATI SVE STUDENTE ---------------------------------------------------
    public List<Student> dohvatiSveStudente() {
        List<Student> studenti = new ArrayList<>();

        String sql = "SELECT st.id_student, st.ime, st.prezime, st.broj_indeksa, " +
                "st.fakultet, st.godina_studija, st.prosjek, st.email, st.telefon, " +
                "st.socijalni_status_id_status, " +
                "ss.id_status, ss.naziv AS naziv_statusa " +
                "FROM student st " +
                "JOIN socijalni_status ss ON st.socijalni_status_id_status = ss.id_status";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student s = new Student();

                s.setIdStudent(rs.getInt("id_student"));
                s.setIme(rs.getString("ime"));
                s.setPrezime(rs.getString("prezime"));
                s.setBrojIndeksa(rs.getString("broj_indeksa"));
                s.setFakultet(rs.getString("fakultet"));
                s.setGodinaStudija(rs.getInt("godina_studija"));
                s.setProsjek(rs.getDouble("prosjek"));
                s.setEmail(rs.getString("email"));
                s.setTelefon(rs.getString("telefon"));

                // SocijalniStatus objekat
                SocijalniStatus ss = new SocijalniStatus(
                        rs.getInt("id_status"),
                        rs.getString("naziv_statusa")
                );
                s.setSocijalniStatus(ss);

                studenti.add(s);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return studenti;
    }

    // DOHVATI STUDENTA PO ID -------------------------------------------------
    public Student dohvatiStudentaPoId(int id) {
        String sql = "SELECT st.id_student, st.ime, st.prezime, st.broj_indeksa, " +
                "st.fakultet, st.godina_studija, st.prosjek, st.email, st.telefon, " +
                "st.socijalni_status_id_status, " +
                "ss.id_status, ss.naziv AS naziv_statusa " +
                "FROM student st " +
                "JOIN socijalni_status ss ON st.socijalni_status_id_status = ss.id_status " +
                "WHERE st.id_student = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student s = new Student();

                    s.setIdStudent(rs.getInt("id_student"));
                    s.setIme(rs.getString("ime"));
                    s.setPrezime(rs.getString("prezime"));
                    s.setBrojIndeksa(rs.getString("broj_indeksa"));
                    s.setFakultet(rs.getString("fakultet"));
                    s.setGodinaStudija(rs.getInt("godina_studija"));
                    s.setProsjek(rs.getDouble("prosjek"));
                    s.setEmail(rs.getString("email"));
                    s.setTelefon(rs.getString("telefon"));

                    SocijalniStatus ss = new SocijalniStatus(
                            rs.getInt("id_status"),
                            rs.getString("naziv_statusa")
                    );
                    s.setSocijalniStatus(ss);

                    return s;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null; // ako nije pronađen
    }

    // AŽURIRANJE STUDENTA ----------------------------------------------------
    public void azurirajStudent(Student s) {
        String sql = "UPDATE student SET " +
                "ime = ?, prezime = ?, broj_indeksa = ?, fakultet = ?, godina_studija = ?, " +
                "prosjek = ?, email = ?, telefon = ?, socijalni_status_id_status = ? " +
                "WHERE id_student = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getIme());
            stmt.setString(2, s.getPrezime());
            stmt.setString(3, s.getBrojIndeksa());
            stmt.setString(4, s.getFakultet());
            stmt.setInt(5, s.getGodinaStudija());
            stmt.setDouble(6, s.getProsjek());
            stmt.setString(7, s.getEmail());
            stmt.setString(8, s.getTelefon());
            stmt.setInt(9, s.getSocijalniStatus().getIdStatus());
            stmt.setInt(10, s.getIdStudent());

            int redovi = stmt.executeUpdate();
            if (redovi > 0)
                System.out.println("Student ažuriran!");
            else
                System.out.println("Student sa ID " + s.getIdStudent() + " ne postoji!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // BRISANJE STUDENTA ------------------------------------------------------
    public void obrisiStudent(int id) {
        String sql = "DELETE FROM student WHERE id_student = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int redovi = stmt.executeUpdate();
            if (redovi > 0)
                System.out.println("Student obrisan!");
            else
                System.out.println("Student sa ID " + id + " ne postoji!");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
