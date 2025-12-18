package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
import dao.PrijavaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();

    private List<VrstaDokumenta> vrsteDokumenata;

    // POZIVA GA GLAVNI CONTROLLER
    public void init(
            int prijavaId,
            int brojClanova,
            List<VrstaDokumenta> vrsteDokumenata
    ) {
        this.prijavaId = prijavaId;
        this.brojClanova = brojClanova;
        this.vrsteDokumenata = vrsteDokumenata;

        generisiSekcijeZaClanove();
    }

    private void generisiSekcijeZaClanove() {
        vboxClanovi.getChildren().clear();
        primanjaPoClanu.clear();
        dokumentiPoClanu.clear();

        for (int i = 1; i <= brojClanova; i++) {
            final int index = i;

            VBox clanBox = new VBox(6);
            clanBox.setStyle("-fx-padding: 8; -fx-border-color: #dddddd;");

            Label lbl = new Label("Član " + index);

            TextField txtIme = new TextField();
            txtIme.setPromptText("Ime / uloga (npr. majka)");

            TextField txtPrimanja = new TextField();
            txtPrimanja.setPromptText("Mjesečna primanja (KM)");

            txtPrimanja.focusedProperty().addListener((o, f, n) -> {
                if (f && !n) {
                    primanjaPoClanu.put(index, parse(txtPrimanja.getText()));
                }
            });

            ComboBox<VrstaDokumenta> cmbVrsta = new ComboBox<>(
                    FXCollections.observableArrayList(vrsteDokumenata)
            );
            cmbVrsta.setPromptText("Vrsta dokumenta");

            cmbVrsta.setCellFactory(c -> new ListCell<>() {
                @Override
                protected void updateItem(VrstaDokumenta v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? "" : v.getNaziv());
                }
            });

            cmbVrsta.setButtonCell(cmbVrsta.getCellFactory().call(null));

            CheckBox chk = new CheckBox("Dokument dostavljen");
            Button btn = new Button("Dodaj dokument");

            dokumentiPoClanu.put(index, new ArrayList<>());

            btn.setOnAction(e -> {
                if (!chk.isSelected() || cmbVrsta.getValue() == null) {
                    alert("Greška", "Popuni sve podatke.");
                    return;
                }

                primanjaPoClanu.put(index, parse(txtPrimanja.getText()));

                Dokument d = new Dokument();
                d.setNaziv(
                        cmbVrsta.getValue().getNaziv() + " - " +
                                (txtIme.getText().isEmpty() ? "Član " + index : txtIme.getText())
                );
                d.setDatumUpload(LocalDate.now());
                d.setBrojBodova(0);
                d.setDostavljen(true);
                d.setVrstaDokumenta(cmbVrsta.getValue());

                new DokumentDAO().unesiDokument(d, prijavaId);
                dokumentiPoClanu.get(index).add(d);
            });

            HBox hbox = new HBox(10, txtPrimanja, cmbVrsta, chk, btn);
            clanBox.getChildren().addAll(lbl, txtIme, hbox);

            vboxClanovi.getChildren().add(clanBox);
        }
    }

    @FXML
    private void zavrsiUnos() {

        for (int i = 1; i <= brojClanova; i++) {
            primanjaPoClanu.putIfAbsent(i, 0.0);
        }

        double ukupno = primanjaPoClanu.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double poClanu = ukupno / brojClanova;

        int bodovi = KriterijPoOsnovuSocijalnogStatusa
                .bodoviZaPrimanja(poClanu);

        new PrijavaDAO().dodajBodoveNaPrijavu(prijavaId, bodovi);

        alert("Gotovo",
                "Ukupna primanja: " + ukupno +
                        "\nPo članu: " + poClanu +
                        "\nBodovi: " + bodovi);
    }

    private double parse(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }

    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}
