package controllers.DodajDokumenteControllers;

import dao.DokumentDAO;
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

    private List<VrstaDokumenta> vrsteDokumenata;

    // ⬇️ BITNO: ovdje čuvamo bodove socijalnog statusa
    private int bodoviSocijalniStatus = 0;

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

        for (int i = 1; i <= brojClanova; i++) {
            final int index = i;

            VBox clanBox = new VBox(6);
            clanBox.setStyle("-fx-padding: 8; -fx-border-color: #dddddd;");

            Label lbl = new Label("Član " + index);

            TextField txtIme = new TextField();
            txtIme.setPromptText("Ime / uloga");

            TextField txtPrimanja = new TextField();
            txtPrimanja.setPromptText("Mjesečna primanja (KM)");
            primanjaFieldovi.put(index, txtPrimanja);

            clanBox.getChildren().addAll(lbl, txtIme, txtPrimanja);
            vboxClanovi.getChildren().add(clanBox);
        }
    }

    @FXML
    public void zavrsiUnos() {
        primanjaPoClanu.clear();

        for (int i = 1; i <= brojClanova; i++) {
            primanjaPoClanu.put(i, parse(primanjaFieldovi.get(i).getText()));
        }

        double ukupno = primanjaPoClanu.values().stream().mapToDouble(Double::doubleValue).sum();
        double poClanu = brojClanova == 0 ? 0 : ukupno / brojClanova;

        // ✅ BODOVI PO PRAVILNIKU
        bodoviSocijalniStatus =
                KriterijPoOsnovuSocijalnogStatusa.bodoviZaPrimanja(poClanu);

        alert("Socijalni status",
                "Ukupna primanja: " + ukupno +
                        "\nPo članu: " + poClanu +
                        "\nBodovi: " + bodoviSocijalniStatus);
    }

    public int getBodoviSocijalniStatus() {
        return bodoviSocijalniStatus;
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
}
