package controllers;

import dao.StudentDAO;
import model.Student;
import dao.DokumentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Dokument;
import model.Prijava;
import javafx.stage.Stage;


public class DetaljiPrijaveController {

    @FXML private Label lblNaslov;
    @FXML private Label lblInfo;
    @FXML private Label lblUkupniBodovi;

    @FXML private TableView<Dokument> tblDokumenti;
    @FXML private TableColumn<Dokument, String> colNaziv;
    @FXML private TableColumn<Dokument, String> colVrsta;
    @FXML private TableColumn<Dokument, String> colDatum;
    @FXML private TableColumn<Dokument, Integer> colBodovi;
    @FXML private TableColumn<Dokument, String> colDostavljen;

    @FXML
    private void onZatvori() {
        Stage stage = (Stage) lblNaslov.getScene().getWindow();
        stage.close();
    }

    private final DokumentDAO dokumentDAO = new DokumentDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public void setPrijava(Prijava prijava) {

        Student student = studentDAO.dohvatiStudentaPoId(prijava.getIdStudent());

        if (student != null) {
            lblNaslov.setText(
                    "Detalji prijave – " +
                            student.getIme() + " " + student.getPrezime()
            );
        } else {
            lblNaslov.setText("Detalji prijave #" + prijava.getIdPrijava());
        }

        lblInfo.setText("Akademska godina: " + prijava.getAkademskaGodina());
        lblUkupniBodovi.setText(
                "Ukupni bodovi: " + prijava.getUkupniBodovi()
        );

        colNaziv.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNaziv())
        );

        colVrsta.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getVrstaDokumenta().getNaziv()
                )
        );

        colDatum.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getDatumUpload().toString()
                )
        );

        colBodovi.setCellValueFactory(
                d -> new javafx.beans.property.SimpleIntegerProperty(
                        d.getValue().getBrojBodova()
                ).asObject()
        );

        colDostavljen.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(
                        d.getValue().isDostavljen() ? "DA" : "NE"
                )
        );

        tblDokumenti.setItems(
                FXCollections.observableArrayList(
                        dokumentDAO.dohvatiDokumenteZaPrijavu(prijava.getIdPrijava())
                )
        );

    }
}
