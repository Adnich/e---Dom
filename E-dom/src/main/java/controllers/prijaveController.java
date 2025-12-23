package controllers;

import dao.PrijavaDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import model.Prijava;


public class prijaveController {

    @FXML private TableView<Prijava> tblPrijave;

    @FXML private TableColumn<Prijava, Integer> colId;
    @FXML private TableColumn<Prijava, String> colIme;
    @FXML private TableColumn<Prijava, String> colPrezime;
    @FXML private TableColumn<Prijava, String> colDatum;
    @FXML private TableColumn<Prijava, Integer> colAkGod;
    @FXML private TableColumn<Prijava, Double> colUkupniBodovi;
    @FXML private TableColumn<Prijava, String> colStatus;
    @FXML private TableColumn<Prijava, String> colNapomena;


    @FXML private TextField txtSearch;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();


    private ObservableList<Prijava> masterList;

    public void refreshTabela() {
        masterList = FXCollections.observableArrayList(prijavaDAO.dohvatiSvePrijave());
        applyFiltering(); // ponovo poveži filter na novu listu
    }

    @FXML
    private void onNovaPrijava() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/novi-student.fxml"));
            Scene scene = new Scene(loader.load());

            var css = getClass().getResource("/styles/prijave.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Nova prijava");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void otvoriDetalje(Prijava prijava) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/detalji-prijave.fxml"));
            Scene scene = new Scene(loader.load());

            DetaljiPrijaveController controller = loader.getController();
            controller.setPrijava(prijava);

            Stage stage = new Stage();
            stage.setTitle("Detalji prijave");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getIdPrijava()).asObject());
        colIme.setCellValueFactory(cd -> new SimpleStringProperty(nullSafe(cd.getValue().getImeStudenta())));
        colPrezime.setCellValueFactory(cd -> new SimpleStringProperty(nullSafe(cd.getValue().getPrezimeStudenta())));

        colDatum.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDatumPrijava() != null ? cd.getValue().getDatumPrijava().toString() : ""
        ));

        colAkGod.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getAkademskaGodina()).asObject());
        colUkupniBodovi.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getUkupniBodovi()).asObject());

        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatusPrijave() != null ? cd.getValue().getStatusPrijave().getNaziv() : ""
        ));

        colNapomena.setCellValueFactory(cd -> new SimpleStringProperty(nullSafe(cd.getValue().getNapomena())));

        masterList = FXCollections.observableArrayList(prijavaDAO.dohvatiSvePrijave());

        applyFiltering();

        tblPrijave.setRowFactory(tv -> {
            TableRow<Prijava> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    otvoriDetalje(row.getItem());
                }
            });
            return row;
        });

        tblPrijave.setTooltip(new Tooltip("Dvoklik na prijavu za pregled detalja"));
    }

    private void applyFiltering() {
        if (masterList == null) return;

        FilteredList<Prijava> filtered = new FilteredList<>(masterList, p -> true);

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                String q = (newVal == null) ? "" : newVal.trim().toLowerCase();

                filtered.setPredicate(p -> {
                    if (q.isEmpty()) return true;

                    String ime = nullSafe(p.getImeStudenta()).toLowerCase();
                    String prezime = nullSafe(p.getPrezimeStudenta()).toLowerCase();
                    String status = (p.getStatusPrijave() != null && p.getStatusPrijave().getNaziv() != null)
                            ? p.getStatusPrijave().getNaziv().toLowerCase()
                            : "";
                    String napomena = nullSafe(p.getNapomena()).toLowerCase();

                    String akGod = String.valueOf(p.getAkademskaGodina());
                    String id = String.valueOf(p.getIdPrijava());

                    return ime.contains(q)
                            || prezime.contains(q)
                            || (ime + " " + prezime).contains(q)
                            || status.contains(q)
                            || napomena.contains(q)
                            || akGod.contains(q)
                            || id.contains(q);
                });
            });
        }

        SortedList<Prijava> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblPrijave.comparatorProperty());

        tblPrijave.setItems(sorted);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
