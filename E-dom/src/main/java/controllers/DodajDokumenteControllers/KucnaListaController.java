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
    private final Map<Integer, TextField> primanjaFieldovi = new HashMap<>();
    private final Map<Integer, List<Dokument>> dokumentiPoClanu = new HashMap<>();

    private List<VrstaDokumenta> vrsteDokumenata;

    /**
     * Inicijalizacija kontrolera
     */
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

    /**
     * Generiše sekcije (UI) za svaki član domaćinstva
     */
    private void generisiSekcijeZaClanove() {
        vboxClanovi.getChildren().clear();
        primanjaPoClanu.clear();
        primanjaFieldovi.clear();
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
            primanjaFieldovi.put(index, txtPrimanja); // zapamti TextField

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

                double iznos = parse(txtPrimanja.getText());
                primanjaPoClanu.put(index, iznos);

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

                alert("Uspjeh", "Dokument dodat za člana " + index);
            });

            HBox hbox = new HBox(10, txtPrimanja, cmbVrsta, chk, btn);
            clanBox.getChildren().addAll(lbl, txtIme, hbox);

            vboxClanovi.getChildren().add(clanBox);
        }
    }

    /**
     * Završava unos i obračunava bodove po primanjima
     */
    @FXML
    public void zavrsiUnos() {
        primanjaPoClanu.clear();

        // Čitanje unesenih iznosa iz TextFieldova
        for (int i = 1; i <= brojClanova; i++) {
            TextField tf = primanjaFieldovi.get(i);
            double iznos = parse(tf.getText());
            primanjaPoClanu.put(i, iznos);
        }

        double ukupno = primanjaPoClanu.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double poClanu = brojClanova == 0 ? 0 : ukupno / brojClanova;

        int bodovi = KriterijPoOsnovuSocijalnogStatusa
                .bodoviZaPrimanja(poClanu);

        new PrijavaDAO().dodajBodoveNaPrijavu(prijavaId, bodovi);

        alert("Gotovo",
                "Ukupna primanja: " + ukupno +
                        "\nPo članu: " + poClanu +
                        "\nBodovi: " + bodovi);
    }

    /**
     * Pomoćna metoda za parsiranje broja iz Stringa
     */
    private double parse(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Pomoćna metoda za prikaz alert dijaloga
     */
    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    public Map<Integer, Double> getPrimanjaPoClanu() {
        return new HashMap<>(primanjaPoClanu); // kopija mape
    }

}
