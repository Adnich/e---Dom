package controllers;

import dao.PrijavaDAO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.Prijava;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.control.TableCell;


public class RangListaController {

    @FXML private TableView<Prijava> tblRangLista;
    @FXML private TableColumn<Prijava, Number> colRedniBroj;
    @FXML private TableColumn<Prijava, String> colImePrezime;
    @FXML private TableColumn<Prijava, Double> colUkupniBodovi;
    @FXML private TableColumn<Prijava, Button> colDetalji;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private ObservableList<Prijava> rangLista;

    @FXML
    public void initialize() {

        // Redni broj
        colRedniBroj.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(tblRangLista.getItems().indexOf(cd.getValue()) + 1));

        // Ime i prezime zajedno
        colImePrezime.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getImeStudenta() + " " + cd.getValue().getPrezimeStudenta()));

        // Ukupni bodovi
        colUkupniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getUkupniBodovi()));

        // Button za detalje
        colDetalji.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("Detalji");

            {
                btn.setOnAction(e -> {
                    Prijava prijava = getTableView().getItems().get(getIndex());
                    otvoriDetalje(prijava);
                });
                btn.setStyle("-fx-background-color: #22b39a; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Dohvati samo odobrene prijave i sortiraj po ukupnim bodovima opadajuće
        rangLista = FXCollections.observableArrayList(
                prijavaDAO.dohvatiSvePrijave().stream()
                        .filter(p -> p.getStatusPrijave() != null && "odobreno".equalsIgnoreCase(p.getStatusPrijave().getNaziv()))
                        .sorted((p1, p2) -> Double.compare(p2.getUkupniBodovi(), p1.getUkupniBodovi()))
                        .toList()
        );

        tblRangLista.setItems(rangLista);
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
            stage.setResizable(true);
            stage.show();

            Platform.runLater(() -> {
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
