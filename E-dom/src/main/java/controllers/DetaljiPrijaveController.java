package controllers;

import dao.StudentDAO;
import model.Student;
import dao.DokumentDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Dokument;
import model.Prijava;
import service.PdfService;

import java.util.List;

public class DetaljiPrijaveController {

    @FXML private Label lblNaslov;
    @FXML private Label lblInfo;

    @FXML private Label lblPrijavaId;
    @FXML private Label lblStatus;

    @FXML private Label lblKompletiranostText;
    @FXML private ProgressBar pbKompletiranost;

    @FXML private Label lblUkupniBodoviValue;
    @FXML private Label lblDostavljeniCount;
    @FXML private Label lblStatusObrade;
    @FXML private Label lblNedostajuCount;

    @FXML private TableView<Dokument> tblDokumenti;
    @FXML private TableColumn<Dokument, String> colNaziv;
    @FXML private TableColumn<Dokument, String> colVrsta;
    @FXML private TableColumn<Dokument, String> colDatum;
    @FXML private TableColumn<Dokument, Integer> colBodovi;
    @FXML private TableColumn<Dokument, String> colDostavljen;
    @FXML private TableColumn<Dokument, Void> colPregled;

    @FXML private VBox emptyState;

    private final DokumentDAO dokumentDAO = new DokumentDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    private void onZatvori() {
        Stage stage = (Stage) lblNaslov.getScene().getWindow();
        stage.close();
    }

    public void setPrijava(Prijava prijava) {

        // ===== HEADER TEXT =====
        Student student = studentDAO.dohvatiStudentaPoId(prijava.getIdStudent());

        if (student != null) {
            lblNaslov.setText("Detalji prijave – " + student.getIme() + " " + student.getPrezime());
        } else {
            lblNaslov.setText("Detalji prijave #" + prijava.getIdPrijava());
        }

        lblInfo.setText("Akademska godina: " + prijava.getAkademskaGodina());
        lblPrijavaId.setText("Prijava #" + prijava.getIdPrijava());

        // Ako imaš status u prijava objektu:
        String statusNaziv = (prijava.getStatusPrijave() != null && prijava.getStatusPrijave().getNaziv() != null)
                ? prijava.getStatusPrijave().getNaziv()
                : "U toku";

        lblStatus.setText(statusNaziv);
        setStatusPillStyle(statusNaziv);

        // ===== BODOVI =====
        lblUkupniBodoviValue.setText(String.valueOf(prijava.getUkupniBodovi()));
        lblStatusObrade.setText(statusNaziv);

        // ===== TABLE CELL VALUE FACTORIES =====
        colNaziv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNaziv()));

        colVrsta.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getVrstaDokumenta() != null ? d.getValue().getVrstaDokumenta().getNaziv() : ""
        ));

        colDatum.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDatumUpload() != null ? d.getValue().getDatumUpload().toString() : ""
        ));

        colBodovi.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getBrojBodova()).asObject());

        // DA/NE value (still needed)
        colDostavljen.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDostavljen() ? "DA" : "NE"
        ));

        // ===== BADGE CELL FACTORY (DA/NE pill) =====
        colDostavljen.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            {
                badge.getStyleClass().add("badge");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    badge.setText(item);

                    badge.getStyleClass().removeAll("badge-yes", "badge-no");
                    badge.getStyleClass().add(item.equalsIgnoreCase("DA") ? "badge-yes" : "badge-no");

                    setGraphic(badge);
                }
            }
        });

        // ===== ACTION BUTTON: Pregledaj =====
        colPregled.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Pregledaj");

            {
                btn.getStyleClass().add("btn-action");
                btn.setOnAction(event -> {
                    Dokument dokument = getTableView().getItems().get(getIndex());
                    if (dokument.getDokumentB64() != null && !dokument.getDokumentB64().isEmpty()) {
                        PdfService.prikaziPdf(dokument.getDokumentB64(), dokument.getNaziv());
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Pregled dokumenta");
                        alert.setHeaderText(null);
                        alert.setContentText("Dokument nije dodan.");
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // ===== LOAD DOCUMENTS =====
        List<Dokument> docs = dokumentDAO.dohvatiDokumenteZaPrijavu(prijava.getIdPrijava());
        tblDokumenti.setItems(FXCollections.observableArrayList(docs));

        // ===== STATS + PROGRESS =====
        updateStatsAndProgress(docs);
        updateEmptyState(docs);
    }

    private void updateStatsAndProgress(List<Dokument> docs) {
        int total = docs.size();
        long dostavljeno = docs.stream().filter(Dokument::isDostavljen).count();
        long nedostaje = total - dostavljeno;

        lblDostavljeniCount.setText(dostavljeno + " / " + total);
        lblNedostajuCount.setText(String.valueOf(nedostaje));

        double progress = total == 0 ? 0 : (double) dostavljeno / total;
        pbKompletiranost.setProgress(progress);
        lblKompletiranostText.setText("Kompletiranost: " + (int)(progress * 100) + "%");
    }

    private void updateEmptyState(List<Dokument> docs) {
        boolean empty = docs == null || docs.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    private void setStatusPillStyle(String status) {
        // ukloni prethodne klase
        lblStatus.getStyleClass().removeAll("status-info", "status-success", "status-warning", "status-danger");

        String s = status.toLowerCase();

        if (s.contains("odob") || s.contains("prihv") || s.contains("potvr")) {
            lblStatus.getStyleClass().add("status-success");
        } else if (s.contains("odbij") || s.contains("neva") || s.contains("gres")) {
            lblStatus.getStyleClass().add("status-danger");
        } else if (s.contains("ček") || s.contains("u toku") || s.contains("obrada")) {
            lblStatus.getStyleClass().add("status-info");
        } else {
            lblStatus.getStyleClass().add("status-warning");
        }
    }
}
