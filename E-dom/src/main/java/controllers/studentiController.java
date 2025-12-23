package controllers;

import dao.StudentDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Student;

public class studentiController {

    @FXML private TableView<Student> tblStudenti;

    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colIme;
    @FXML private TableColumn<Student, String> colPrezime;
    @FXML private TableColumn<Student, String> colIndeks;
    @FXML private TableColumn<Student, String> colFakultet;
    @FXML private TableColumn<Student, Integer> colGodina;
    @FXML private TableColumn<Student, Double> colProsjek;
    @FXML private TableColumn<Student, String> colStatus;

    @FXML private TextField txtSearch;

    private final StudentDAO studentDAO = new StudentDAO();

    private ObservableList<Student> masterList;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getIdStudent()).asObject());
        colIme.setCellValueFactory(cell -> new SimpleStringProperty(nullSafe(cell.getValue().getIme())));
        colPrezime.setCellValueFactory(cell -> new SimpleStringProperty(nullSafe(cell.getValue().getPrezime())));
        colIndeks.setCellValueFactory(cell -> new SimpleStringProperty(nullSafe(cell.getValue().getBrojIndeksa())));
        colFakultet.setCellValueFactory(cell -> new SimpleStringProperty(nullSafe(cell.getValue().getFakultet())));
        colGodina.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getGodinaStudija()).asObject());
        colProsjek.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getProsjek()).asObject());
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getSocijalniStatus() != null
                        ? nullSafe(cell.getValue().getSocijalniStatus().getNaziv())
                        : ""
        ));

        masterList = FXCollections.observableArrayList(studentDAO.dohvatiSveStudente());

        applyFiltering();
    }

    private void applyFiltering() {
        if (masterList == null) return;

        FilteredList<Student> filtered = new FilteredList<>(masterList, s -> true);

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                String q = (newVal == null) ? "" : newVal.trim().toLowerCase();

                filtered.setPredicate(s -> {
                    if (q.isEmpty()) return true;

                    String ime = nullSafe(s.getIme()).toLowerCase();
                    String prezime = nullSafe(s.getPrezime()).toLowerCase();
                    String indeks = nullSafe(s.getBrojIndeksa()).toLowerCase();
                    String fakultet = nullSafe(s.getFakultet()).toLowerCase();
                    String status = (s.getSocijalniStatus() != null && s.getSocijalniStatus().getNaziv() != null)
                            ? s.getSocijalniStatus().getNaziv().toLowerCase()
                            : "";

                    String godina = String.valueOf(s.getGodinaStudija());
                    String id = String.valueOf(s.getIdStudent());

                    return ime.contains(q)
                            || prezime.contains(q)
                            || (ime + " " + prezime).contains(q)
                            || indeks.contains(q)
                            || fakultet.contains(q)
                            || status.contains(q)
                            || godina.contains(q)
                            || id.contains(q);
                });
            });
        }

        SortedList<Student> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblStudenti.comparatorProperty());

        tblStudenti.setItems(sorted);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
