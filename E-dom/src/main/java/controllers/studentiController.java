package controllers;

import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import model.Student;

public class studentiController {

    @FXML
    private TableView<Student> tblStudenti;

    @FXML
    private TableColumn<Student, Integer> colId;

    @FXML
    private TableColumn<Student, String> colIme;

    @FXML
    private TableColumn<Student, String> colPrezime;

    @FXML
    private TableColumn<Student, String> colIndeks;

    @FXML
    private TableColumn<Student, String> colFakultet;

    @FXML
    private TableColumn<Student, Integer> colGodina;

    @FXML
    private TableColumn<Student, Double> colProsjek;

    @FXML
    private TableColumn<Student, String> colStatus;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(
                cell -> new SimpleIntegerProperty(cell.getValue().getIdStudent()).asObject()
        );

        colIme.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getIme())
        );

        colPrezime.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getPrezime())
        );

        colIndeks.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getBrojIndeksa())
        );

        colFakultet.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getFakultet())
        );

        colGodina.setCellValueFactory(
                cell -> new SimpleIntegerProperty(cell.getValue().getGodinaStudija()).asObject()
        );

        colProsjek.setCellValueFactory(
                cell -> new SimpleDoubleProperty(cell.getValue().getProsjek()).asObject()
        );

        colStatus.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getSocijalniStatus() != null
                                ? cell.getValue().getSocijalniStatus().getNaziv()
                                : ""
                )
        );

        // učitaj sve studente iz baze
        tblStudenti.setItems(
                FXCollections.observableArrayList(studentDAO.dohvatiSveStudente())
        );
    }
}
