package controllers;

import dao.PrijavaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import model.Prijava;

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
    private TableColumn<Prijava, Integer> colUkupniBodovi;

    @FXML
    private TableColumn<Prijava, String> colStatus;

    @FXML
    private TableColumn<Prijava, String> colNapomena;

    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    @FXML
    public void initialize() {

        // poveži kolone sa poljima modela
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
                cellData -> new SimpleIntegerProperty(cellData.getValue().getUkupniBodovi()).asObject()
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

        // učitaj podatke iz baze
        tblPrijave.setItems(
                FXCollections.observableArrayList(prijavaDAO.dohvatiSvePrijave())
        );
    }
}
