package controllers;

import dao.PrijavaDAO;
import dto.RangListaDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.Prijava;
import service.export.RangListaHtmlExportService;
import util.BodoviUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RangListaController {

    @FXML private TableView<Prijava> tblRangLista;
    @FXML private TableColumn<Prijava, Number> colRedniBroj;
    @FXML private TableColumn<Prijava, Integer> colIdPrijave;
    @FXML private TableColumn<Prijava, String> colImePrezime;
    @FXML private TableColumn<Prijava, Double> colUkupniBodovi;
    @FXML private TableColumn<Prijava, Double> colGodinaStudija;
    @FXML private TableColumn<Prijava, Double> colProsjek;
    @FXML private TableColumn<Prijava, Double> colOsvojeneNagrade;
    @FXML private TableColumn<Prijava, Double> colSocijalniStatus;
    @FXML private TableColumn<Prijava, Double> colUdaljenost;
    @FXML private TableColumn<Prijava, Double> colDodatniBodovi;
    @FXML private TableColumn<Prijava, Void> colDetalji;
    @FXML private Menu menuKolone;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();
    private ObservableList<Prijava> rangLista;

    @FXML
    public void initialize() {

        // ==================== PRIPREMA KOLONA I MENIJA ====================
        Map<String, TableColumn<Prijava, ?>> koloneMap = Map.of(
                "Redni broj", colRedniBroj,
                "ID prijave", colIdPrijave,
                "Ime i prezime", colImePrezime,
                "Ukupni bodovi", colUkupniBodovi,
                "Godina studija", colGodinaStudija,
                "Prosjek", colProsjek,
                "Osvojene nagrade", colOsvojeneNagrade,
                "Socijalni status", colSocijalniStatus,
                "Udaljenost", colUdaljenost,
                "Dodatni bodovi", colDodatniBodovi
        );

        menuKolone.getItems().clear();
        koloneMap.forEach((naziv, kolona) -> {
            CheckMenuItem item = new CheckMenuItem(naziv);
            item.setSelected(true); // default sve kolone vidljive
            item.selectedProperty().addListener((obs, wasSelected, isSelected) -> kolona.setVisible(isSelected));
            menuKolone.getItems().add(item);
        });

        // ==================== SETUP TABLECOLUMNS ====================

        // Redni broj koji prati sortiranu listu
        colRedniBroj.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(tblRangLista.getItems().indexOf(cd.getValue()) + 1)
        );

        // ID prijave
        colIdPrijave.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getIdPrijava())
        );

        // Ime i prezime
        colImePrezime.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getImeStudenta() + " " + cd.getValue().getPrezimeStudenta())
        );

        // Godina studija
        colGodinaStudija.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("godina", 0.0))
        );

        // Prosjek
        colProsjek.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("uspjeh", 0.0))
        );

        // Osvojene nagrade
        colOsvojeneNagrade.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("nagrade", 0.0))
        );

        // Socijalni status
        colSocijalniStatus.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("socijalni", 0.0))
        );

        // Udaljenost
        colUdaljenost.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("udaljenost", 0.0))
        );

        // Dodatni bodovi
        colDodatniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("dodatni", 0.0))
        );

        // Ukupni bodovi
        colUkupniBodovi.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getBodoviMap().getOrDefault("ukupno", 0.0))
        );

        // Dugme za detalje
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

        // ==================== UCITAVANJE RANGLISTE ====================
        rangLista = FXCollections.observableArrayList();
        for (Prijava p : prijavaDAO.dohvatiSvePrijave()) {
            if (p.getStatusPrijave() != null && "odobreno".equalsIgnoreCase(p.getStatusPrijave().getNaziv())) {
                Map<String, Double> bodovi = BodoviUtil.izracunajBodoveZaPrijavu(p);
                p.setBodoviMap(bodovi); // polje mora postojati u Prijava modelu
                rangLista.add(p);
            }
        }

        // ==================== SORTIRANJE ====================
        SortedList<Prijava> sorted = new SortedList<>(rangLista);
        sorted.comparatorProperty().bind(tblRangLista.comparatorProperty());
        tblRangLista.setItems(sorted);

// Inicijalno sortiranje po ukupnim bodovima silazno
        colUkupniBodovi.setSortType(TableColumn.SortType.DESCENDING);
        tblRangLista.getSortOrder().clear();
        tblRangLista.getSortOrder().add(colUkupniBodovi);

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

    @FXML
    public void onExportHtmlPdf(ActionEvent actionEvent) {
        try {
            List<RangListaDTO> exportLista = pripremiZaExport();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sačuvaj rang listu kao PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fajl", "*.pdf"));
            File file = fileChooser.showSaveDialog(tblRangLista.getScene().getWindow());

            if (file != null) {
                RangListaHtmlExportService service = new RangListaHtmlExportService();
                service.exportData(exportLista, file);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF uspješno kreiran!", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Greška pri kreiranju PDF-a: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }


    private List<RangListaDTO> pripremiZaExport() {
        List<RangListaDTO> exportLista = new ArrayList<>();

        for (Prijava p : tblRangLista.getItems()) {
            RangListaDTO dto = new RangListaDTO();

            if (colRedniBroj.isVisible())
                dto.addKolona("R.br", String.valueOf(tblRangLista.getItems().indexOf(p) + 1));

            if (colIdPrijave.isVisible())
                dto.addKolona("ID prijave", String.valueOf(p.getIdPrijava()));

            if (colImePrezime.isVisible())
                dto.addKolona("Ime i prezime", p.getImeStudenta() + " " + p.getPrezimeStudenta());

            if (colUkupniBodovi.isVisible())
                dto.addKolona("Ukupni bodovi", String.valueOf(p.getBodoviMap().getOrDefault("ukupno", 0.0)));

            if (colGodinaStudija.isVisible())
                dto.addKolona("Godina studija", String.valueOf(p.getBodoviMap().getOrDefault("godina", 0.0)));

            if (colProsjek.isVisible())
                dto.addKolona("Prosjek", String.valueOf(p.getBodoviMap().getOrDefault("uspjeh", 0.0)));

            if (colOsvojeneNagrade.isVisible())
                dto.addKolona("Nagrade", String.valueOf(p.getBodoviMap().getOrDefault("nagrade", 0.0)));

            if (colSocijalniStatus.isVisible())
                dto.addKolona("Socijalni", String.valueOf(p.getBodoviMap().getOrDefault("socijalni", 0.0)));

            if (colUdaljenost.isVisible())
                dto.addKolona("Udaljenost", String.valueOf(p.getBodoviMap().getOrDefault("udaljenost", 0.0)));

            if (colDodatniBodovi.isVisible())
                dto.addKolona("Dodatni bodovi", String.valueOf(p.getBodoviMap().getOrDefault("dodatni", 0.0)));

            exportLista.add(dto);
        }

        return exportLista;
    }

}
