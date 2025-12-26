package controllers;

import dao.PrijavaDAO;
import dao.StatusPrijaveDAO;
import dao.StudentDAO;
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
import model.StatusPrijave;
import model.Student;
import service.PdfService;

import java.util.List;

public class DetaljiPrijaveController {

    // ===== HEADER =====
    @FXML private Label lblNaslov;
    @FXML private Label lblInfo;
    @FXML private Label lblPrijavaId;
    @FXML private Label lblStatus;

    // ===== PROGRESS =====
    @FXML private Label lblKompletiranostText;
    @FXML private ProgressBar pbKompletiranost;

    // ===== SUMMARY =====
    @FXML private Label lblUkupniBodoviValue;
    @FXML private Label lblDostavljeniCount;
    @FXML private Label lblStatusObrade;
    @FXML private Label lblNedostajuCount;

    // ===== TABLE =====
    @FXML private TableView<Dokument> tblDokumenti;
    @FXML private TableColumn<Dokument, String> colNaziv;
    @FXML private TableColumn<Dokument, String> colVrsta;
    @FXML private TableColumn<Dokument, String> colDatum;
    @FXML private TableColumn<Dokument, Integer> colBodovi;
    @FXML private TableColumn<Dokument, String> colDostavljen;
    @FXML private TableColumn<Dokument, Void> colPregled;

    // ===== STATUS CONTROL =====
    @FXML private ComboBox<StatusPrijave> cmbStatusPrijave;
    @FXML private Button btnSacuvajStatus;

    @FXML private VBox emptyState;

    // ===== DAO =====
    private final DokumentDAO dokumentDAO = new DokumentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final PrijavaDAO prijavaDAO = new PrijavaDAO();

    private Prijava trenutnaPrijava;

    // =========================
    // ZATVARANJE
    // =========================
    @FXML
    private void onZatvori() {
        ((Stage) lblNaslov.getScene().getWindow()).close();
    }

    // =========================
    // SET PRIJAVA
    // =========================
    public void setPrijava(Prijava prijava) {
        this.trenutnaPrijava = prijava;

        // ===== STUDENT =====
        Student student = studentDAO.dohvatiStudentaPoId(prijava.getIdStudent());
        lblNaslov.setText(student != null
                ? "Detalji prijave – " + student.getIme() + " " + student.getPrezime()
                : "Detalji prijave #" + prijava.getIdPrijava());

        lblInfo.setText("Akademska godina: " + prijava.getAkademskaGodina());
        lblPrijavaId.setText("Prijava #" + prijava.getIdPrijava());

        // ===== STATUS =====
        String statusNaziv = prijava.getStatusPrijave() != null
                ? prijava.getStatusPrijave().getNaziv()
                : "na pregledu";

        lblStatus.setText(statusNaziv);
        lblStatusObrade.setText(statusNaziv);
        setStatusPillStyle(statusNaziv);

        // ===== STATUS COMBO =====
        inicijalizujStatusCombo(prijava.getStatusPrijave());

        // ===== BODOVI =====
        lblUkupniBodoviValue.setText(String.valueOf(prijava.getUkupniBodovi()));

        // ===== TABLE =====
        inicijalizujTabelu();

        List<Dokument> docs = dokumentDAO.dohvatiDokumenteZaPrijavu(prijava.getIdPrijava());
        tblDokumenti.setItems(FXCollections.observableArrayList(docs));

        updateStatsAndProgress(docs);
        updateEmptyState(docs);
    }

    // =========================
    // STATUS COMBO
    // =========================
    private void inicijalizujStatusCombo(StatusPrijave trenutni) {

        StatusPrijaveDAO spDao = new StatusPrijaveDAO();
        List<StatusPrijave> statusi = spDao.dohvatiSveStatuse();
        cmbStatusPrijave.setItems(FXCollections.observableArrayList(statusi));

        // postavi CellFactory da prikazuje samo naziv statusa
        cmbStatusPrijave.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(StatusPrijave item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNaziv()); // prikazuje samo naziv
            }
        });

        // postavi ButtonCell isto tako
        cmbStatusPrijave.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StatusPrijave item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNaziv()); // prikazuje samo naziv
            }
        });

        // postavi trenutni status ako postoji
        if (trenutni != null) {
            cmbStatusPrijave.getItems().stream()
                    .filter(s -> s.getIdStatus() == trenutni.getIdStatus())
                    .findFirst()
                    .ifPresent(cmbStatusPrijave::setValue);
        }
    }


    // =========================
    // SAVE STATUS
    // =========================
    @FXML
    private void onSacuvajStatus() {
        StatusPrijave noviStatus = cmbStatusPrijave.getValue();

        if (noviStatus == null || trenutnaPrijava == null) return;

        prijavaDAO.promijeniStatusPrijave(trenutnaPrijava.getIdPrijava(), noviStatus);

        trenutnaPrijava.setStatusPrijave(noviStatus);
        lblStatus.setText(noviStatus.getNaziv());
        lblStatusObrade.setText(noviStatus.getNaziv());
        setStatusPillStyle(noviStatus.getNaziv());

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Status prijave");
        a.setHeaderText(null);
        a.setContentText("Status prijave je uspješno promijenjen.");
        a.showAndWait();
    }

    // =========================
    // TABLE SETUP
    // =========================
    private void inicijalizujTabelu() {

        colNaziv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNaziv()));
        colVrsta.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getVrstaDokumenta() != null ? d.getValue().getVrstaDokumenta().getNaziv() : ""
        ));
        colDatum.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDatumUpload() != null ? d.getValue().getDatumUpload().toString() : ""
        ));
        colBodovi.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getBrojBodova()).asObject());
        colDostavljen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isDostavljen() ? "DA" : "NE"));

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
                    badge.getStyleClass().add(item.equals("DA") ? "badge-yes" : "badge-no");
                    setGraphic(badge);
                }
            }
        });

        colPregled.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Pregledaj");
            {
                btn.getStyleClass().add("btn-action");
                btn.setOnAction(e -> {
                    Dokument d = getTableView().getItems().get(getIndex());
                    if (d.getDokumentB64() != null && !d.getDokumentB64().isEmpty()) {
                        PdfService.prikaziPdf(d.getDokumentB64(), d.getNaziv());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // =========================
    // STATS
    // =========================
    private void updateStatsAndProgress(List<Dokument> docs) {
        int total = docs.size();
        long dostavljeno = docs.stream().filter(Dokument::isDostavljen).count();
        long nedostaje = total - dostavljeno;

        lblDostavljeniCount.setText(dostavljeno + " / " + total);
        if (lblNedostajuCount != null) {
            lblNedostajuCount.setText(String.valueOf(nedostaje));
        }


        double progress = total == 0 ? 0 : (double) dostavljeno / total;
        pbKompletiranost.setProgress(progress);
        lblKompletiranostText.setText("Kompletiranost: " + (int) (progress * 100) + "%");
    }

    private void updateEmptyState(List<Dokument> docs) {
        boolean empty = docs == null || docs.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    // =========================
    // STATUS STYLE
    // =========================
    private void setStatusPillStyle(String status) {
        lblStatus.getStyleClass().removeAll(
                "status-info", "status-success", "status-warning", "status-danger"
        );

        String s = status.toLowerCase();

        if (s.contains("odob")) {
            lblStatus.getStyleClass().add("status-success");
        } else if (s.contains("odbij")) {
            lblStatus.getStyleClass().add("status-danger");
        } else if (s.contains("zaklj")) {
            lblStatus.getStyleClass().add("status-warning");
        } else {
            lblStatus.getStyleClass().add("status-info");
        }
    }
}
