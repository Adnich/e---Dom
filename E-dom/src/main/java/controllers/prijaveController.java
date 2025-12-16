package controllers;

import dao.PrijavaDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Prijava;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;


public class prijaveController {

    @FXML
    private TableView<Prijava> tblPrijave;

    @FXML
    private TableColumn<Prijava, Integer> colId;

    @FXML
    private TableColumn<Prijava, String> colDatum;

    @FXML
    private TableColumn<Prijava, Integer> colAkGod;

    @FXML
    private TableColumn<Prijava, Double> colUkupniBodovi;

    @FXML
    private TableColumn<Prijava, String> colStatus;

    @FXML
    private TableColumn<Prijava, String> colNapomena;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    // ===============================
    //  OVO JE NOVA METODA (BITNO)
    // ===============================
    public void refreshTabela() {
        tblPrijave.setItems(
                FXCollections.observableArrayList(prijavaDAO.dohvatiSvePrijave())
        );
    }

    @FXML
    private void onNovaPrijava() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/novi-student.fxml"));
            Scene scene = new Scene(loader.load());

            // CSS za novi-student prozor (ostaje kako si imao)
            var css = getClass().getResource("/styles/prijave.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = new Stage();
            stage.setTitle("Nova prijava");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onVidiDetalje() {
        Prijava selektovana = tblPrijave.getSelectionModel().getSelectedItem();

        if (selektovana != null) {
            otvoriDetalje(selektovana);
        }
    }

    private void otvoriDetalje(Prijava prijava) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/detalji-prijave.fxml")
            );
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

        colId.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getIdPrijava()).asObject()
        );

        colDatum.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getDatumPrijava() != null
                                ? cellData.getValue().getDatumPrijava().toString()
                                : ""
                )
        );

        colAkGod.setCellValueFactory(
                cellData -> new SimpleIntegerProperty(cellData.getValue().getAkademskaGodina()).asObject()
        );

        colUkupniBodovi.setCellValueFactory(
                cellData -> new SimpleDoubleProperty(cellData.getValue().getUkupniBodovi()).asObject()
        );

        colStatus.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getStatusPrijave() != null
                                ? cellData.getValue().getStatusPrijave().getNaziv()
                                : ""
                )
        );

        colNapomena.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getNapomena() != null
                                ? cellData.getValue().getNapomena()
                                : ""
                )
        );

        tblPrijave.setItems(
                FXCollections.observableArrayList(prijavaDAO.dohvatiSvePrijave())
        );

        tblPrijave.setRowFactory(tv -> {
            TableRow<Prijava> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Prijava prijava = row.getItem();
                    otvoriDetalje(prijava);
                }
            });

            return row;
        });

        tblPrijave.setTooltip(
                new Tooltip("Dvoklik na prijavu za pregled detalja")
        );


    }
}
