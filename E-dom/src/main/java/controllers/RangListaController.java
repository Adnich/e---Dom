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
import util.BodoviUtil;

import java.util.Map;

public class RangListaController {

    @FXML private TableView<Prijava> tblRangLista;
    @FXML private TableColumn<Prijava, Number> colRedniBroj;
    @FXML private TableColumn<Prijava, String> colImePrezime;
    @FXML private TableColumn<Prijava, Double> colUkupniBodovi;
    @FXML private TableColumn<Prijava, Double> colGodinaStudija;
    @FXML private TableColumn<Prijava, Double> colProsjek;
    @FXML private TableColumn<Prijava, Double> colOsvojeneNagrade;
    @FXML private TableColumn<Prijava, Double> colSocijalniStatus;
    @FXML private TableColumn<Prijava, Double> colUdaljenost;
    @FXML private TableColumn<Prijava, Double> colDodatniBodovi;
    @FXML private TableColumn<Prijava, Void> colDetalji;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private ObservableList<Prijava> rangLista;

    @FXML
    public void initialize() {

        // Redni broj
        colRedniBroj.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(tblRangLista.getItems().indexOf(cd.getValue()) + 1)
        );

        // Ime i prezime
        colImePrezime.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getImeStudenta() + " " + cd.getValue().getPrezimeStudenta())
        );

        // Godina studija
        colGodinaStudija.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("godina"))
        );

        colProsjek.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("uspjeh"))
        );

        // Osvojene nagrade
        colOsvojeneNagrade.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("nagrade"))
        );

        // Socijalni status
        colSocijalniStatus.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("socijalni"))
        );

        // Udaljenost
        colUdaljenost.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("udaljenost"))
        );

        // Dodatni bodovi
        colDodatniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("dodatni"))
        );

        // Ukupni bodovi
        colUkupniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().get("ukupno"))
        );

        // Detalji dugme
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

        // Učitavanje rang liste
        rangLista = FXCollections.observableArrayList();
        for (Prijava p : prijavaDAO.dohvatiSvePrijave()) {
            if (p.getStatusPrijave() != null && "odobreno".equalsIgnoreCase(p.getStatusPrijave().getNaziv())) {
                Map<String, Double> bodovi = BodoviUtil.izracunajBodoveZaPrijavu(p);
                p.setBodoviMap(bodovi); // <-- ovo polje moraš imati u modelu Prijava
                rangLista.add(p);
            }
        }
        tblRangLista.setItems(rangLista);

        // Spriječi plavu selekciju
        tblRangLista.getSelectionModel().setCellSelectionEnabled(false);
    }

    private void otvoriDetalje(Prijava prijava) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/detalji-prijave.fxml"));
            Scene scene = new Scene(loader.load());
            controllers.DetaljiPrijaveController controller = loader.getController();
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
