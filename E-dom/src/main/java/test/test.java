package test;

import dao.KorisnikDAO;
import dao.SocijalniStatusDAO;
import dao.StudentDAO;
import dao.UlogaDAO;
import model.Korisnik;
import model.SocijalniStatus;
import model.Student;
import model.Uloga;

import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String[] args) {

        UlogaDAO uDao = new UlogaDAO();

        Uloga u = new Uloga();

        KorisnikDAO dao = new KorisnikDAO();

        Korisnik k = new Korisnik();

        u.setNaziv("ADMIN");

        k.setIme("Hamo");
        k.setPrezime("Hamić");
        k.setUsername("hamo.hamic");
        k.setPasswordHash("12345");
        k.setUloga(u);

        //uDao.unesiUlogu(u);

        //dao.unesiKorisnika(k);

       // uDao.obrisiUlogu(1);

        SocijalniStatusDAO sDao = new SocijalniStatusDAO();

        SocijalniStatus s = new SocijalniStatus();

        //s.setNaziv("zaposlen3");
        //sDao.unesiStatus(s);


        StudentDAO studentDAO  = new StudentDAO();
        Student student = new Student();

        student.setIme("hkjh");
        student.setPrezime("jhhj");
        student.setBrojIndeksa("543");
        student.setGodinaStudija(3);
        student.setFakultet("Politehnički");
        student.setSocijalniStatus(s);
        student.setProsjek(8.5);
        student.setEmail("ujgjgjgjg.23@size.ba");

        //studentDAO.unesiStudent(student);

        List<Student> studenti = new ArrayList<>();

        studenti = studentDAO.dohvatiSveStudente();

        studenti.forEach(System.out::println);
    }
}
