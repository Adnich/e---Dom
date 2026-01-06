package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import dao.PrijavaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Dokument;
import model.VrstaDokumenta;
import service.KriterijPoOsnovuSocijalnogStatusa;

import java.time.LocalDate;
import java.util.*;

public class KucnaListaController {

    @FXML
    private VBox vboxClanovi;

    private int prijavaId;
    private int brojClanova;

    private final Map<Integer, Double> primanjaPoClanu = new HashMap<>();
    private final Map<Integer, TextField> primanjaFieldovi = new HashMap<>();
    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();

    private List<VrstaDokumenta> vrsteDokumenata;

    public void init(int prijavaId, int brojClanova, List<VrstaDokumenta> vrsteDokumenata) {
        this.prijavaId = prijavaId;
        this.brojClanova = brojClanova;
        this.vrsteDokumenata = vrsteDokumenata;

        generisiSekcijeZaClanove();
    }

    private void generisiSekcijeZaClanove() {
        vboxClanovi.getChildren().clear();
        primanjaPoClanu.clear();
        primanjaFieldovi.clear();
        dokumentiPoClanu.clear();

        for (int i = 1; i <= brojClanova-1; i++) {
            final int index = i;

            VBox clanBox = new VBox(12);
            clanBox.getStyleClass().add("household-member-card");

            Label lbl = new Label("Član " + index);
            lbl.getStyleClass().add("member-title");

            TextField txtIme = new TextField();
            txtIme.setPromptText("Ime / uloga (npr. majka)");
            txtIme.getStyleClass().add("field");
            txtIme.setMaxWidth(Double.MAX_VALUE);

            // ✅ Primanja (uvijek vidljivo)
            TextField txtPrimanja = new TextField();
            txtPrimanja.setPromptText("Mjesečna primanja (KM)");
            txtPrimanja.getStyleClass().add("field");
            txtPrimanja.setMinWidth(220);
            txtPrimanja.setPrefWidth(230);

            primanjaFieldovi.put(index, txtPrimanja);

            // ✅ ComboBox
            ComboBox<VrstaDokumenta> cmbVrsta = new ComboBox<>(
                    FXCollections.observableArrayList(vrsteDokumenata)
            );
            cmbVrsta.setPromptText("Vrsta dokumenta");
            cmbVrsta.getStyleClass().add("combo");
            cmbVrsta.setMaxWidth(Double.MAX_VALUE);

            cmbVrsta.setCellFactory(c -> new ListCell<>() {
                @Override
                protected void updateItem(VrstaDokumenta v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? "" : v.getNaziv());
                }
            });
            cmbVrsta.setButtonCell(cmbVrsta.getCellFactory().call(null));

            // ✅ Checkbox ispod
            CheckBox chk = new CheckBox("Dokument dostavljen");
            chk.getStyleClass().add("checkbox");
            chk.setWrapText(true);

            // ✅ Button
            Button btn = new Button("Dodaj dokument");
            btn.getStyleClass().add("btn-upload");
            btn.setMinWidth(170);
            btn.setPrefWidth(170);

            dokumentiPoClanu.put(index, new ArrayList<>());

            btn.setOnAction(e -> {
                if (!chk.isSelected() || cmbVrsta.getValue() == null) {
                    alert("Greška", "Popuni sve podatke.");
                    return;
                }

                double iznos = parse(txtPrimanja.getText());
                primanjaPoClanu.put(index, iznos);

                Dokument d = new Dokument();
                d.setNaziv(
                        cmbVrsta.getValue().getNaziv() + " - " +
                                (txtIme.getText().isEmpty()
                                        ? "Član " + index
                                        : txtIme.getText())
                );
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0);
                d.setDostavljen(true);
                d.setVrstaDokumenta(cmbVrsta.getValue());

                new DokumentDAO().unesiDokument(d, prijavaId);
                dokumentiPoClanu.get(index).add(d);

                alert("Uspjeh", "Dokument dodat za člana " + index);
            });

            // ✅ GRID ROW (3 kolone)
            GridPane row1 = new GridPane();
            row1.setHgap(14);
            row1.setVgap(0);
            row1.setAlignment(Pos.CENTER_LEFT);
            row1.setMaxWidth(Double.MAX_VALUE);

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setMinWidth(220);
            col1.setPrefWidth(230);
            col1.setHgrow(Priority.NEVER);

            ColumnConstraints col2 = new ColumnConstraints();
            col2.setHgrow(Priority.ALWAYS);
            col2.setFillWidth(true);

            ColumnConstraints col3 = new ColumnConstraints();
            col3.setMinWidth(170);
            col3.setPrefWidth(170);
            col3.setHgrow(Priority.NEVER);

            row1.getColumnConstraints().addAll(col1, col2, col3);

            row1.add(txtPrimanja, 0, 0);
            row1.add(cmbVrsta, 1, 0);
            row1.add(btn, 2, 0);

            // ✅ Checkbox row
            HBox row2 = new HBox(10, chk);
            row2.setAlignment(Pos.CENTER_LEFT);

            clanBox.getChildren().addAll(lbl, txtIme, row1, row2);
            vboxClanovi.getChildren().add(clanBox);
        }
    }

    @FXML
    public double zavrsiUnos() {
        primanjaPoClanu.clear();

        // Čitanje unesenih iznosa iz TextFieldova
        for (int i = 1; i <= brojClanova-1; i++) {
            TextField tf = primanjaFieldovi.get(i);
            double iznos = parse(tf.getText());
            primanjaPoClanu.put(i, iznos);
        }

        double ukupno = primanjaPoClanu.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double poClanu = brojClanova == 0 ? 0 : ukupno / brojClanova;

        double bodovi = KriterijPoOsnovuSocijalnogStatusa
                .bodoviZaPrimanja(poClanu);

        new PrijavaDAO().dodajBodoveNaPrijavu(prijavaId, bodovi);

        alert("Gotovo",
                "Ukupna primanja: " + ukupno +
                        "\nPo članu: " + poClanu +
                        "\nBodovi: " + bodovi);

        return bodovi;
    }

    private double parse(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    public Map<Integer, Double> getPrimanjaPoClanu() {
        return new HashMap<>(primanjaPoClanu);
    }
}
