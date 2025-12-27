package controllers;

import dao.PrijavaDAO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Prijava;

public class RangListaController {

    @FXML private TableView<Prijava> tblRangLista;
    @FXML private TableColumn<Prijava, Number> colRedniBroj;
    @FXML private TableColumn<Prijava, String> colImePrezime;
    @FXML private TableColumn<Prijava, Double> colUkupniBodovi;
    @FXML private TableColumn<Prijava, Void> colDetalji;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private ObservableList<Prijava> rangLista;

    @FXML
    public void initialize() {

        /* =========================
           CELL VALUE FACTORIES
        ========================= */

        // Redni broj - stabilno i brzo
        colRedniBroj.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(tblRangLista.getItems().indexOf(cd.getValue()) + 1)
        );

        // Ime i prezime
        colImePrezime.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getImeStudenta() + " " + cd.getValue().getPrezimeStudenta())
        );

        // Ukupni bodovi
        colUkupniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getUkupniBodovi())
        );

        /* =========================
           BEAUTIFY CELLS
        ========================= */

        // Redni broj: centar + bold
        colRedniBroj.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove("rbr-cell");
                } else {
                    setText(String.valueOf(item.intValue()));
                    if (!getStyleClass().contains("rbr-cell"))
                        getStyleClass().add("rbr-cell");
                }
            }
        });

        // Bodovi: format + centar + highlight
        colUkupniBodovi.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    getStyleClass().remove("bodovi-cell");
                } else {
                    setText(String.format("%.1f", item));
                    if (!getStyleClass().contains("bodovi-cell"))
                        getStyleClass().add("bodovi-cell");
                }
            }
        });

        // Detalji dugme - koristi CSS klasu
        colDetalji.setCellFactory(tc -> new TableCell<>() {

            private final Button btn = new Button("Detalji");

            {
                btn.getStyleClass().add("det-btn");

                btn.setOnAction(e -> {
                    Prijava prijava = getTableView().getItems().get(getIndex());
                    otvoriDetalje(prijava);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        /* =========================
           LOAD RANK LIST
        ========================= */

        rangLista = FXCollections.observableArrayList(
                prijavaDAO.dohvatiSvePrijave().stream()
                        .filter(p -> p.getStatusPrijave() != null
                                && "odobreno".equalsIgnoreCase(p.getStatusPrijave().getNaziv()))
                        .sorted((p1, p2) -> Double.compare(p2.getUkupniBodovi(), p1.getUkupniBodovi()))
                        .toList()
        );

        tblRangLista.setItems(rangLista);

        // BONUS: spriječi "selektovanje plavom" (ako želiš čist UI)
        tblRangLista.getSelectionModel().setCellSelectionEnabled(false);
    }

    /* =========================
       OPEN DETAILS (FULLSCREEN)
    ========================= */

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

            Platform.runLater(() -> maximize(stage));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void maximize(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
    }
}
