package controllers.DodajDokumenteControllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import service.KriterijPoOsnovuDjeceBranilaca;
import service.PdfService;
import service.PdfService;
import java.util.HashMap;
import java.util.Map;

public class BraniociDokumentiController {

    @FXML
    private CheckBox chkStudentRVI;

    @FXML
    private TextField txtPostotakInvaliditeta;

    @FXML
    private CheckBox chkInvaliditet;

    @FXML
    private CheckBox chkDjecaSehida;

    @FXML
    private CheckBox chkClanPorodiceSehida;

    @FXML
    private CheckBox chkInvalidnostRoditelja;

    @FXML
    private TextField txtPostInvalidnostiRoditelja;

    @FXML
    private CheckBox chkDjecaOSRBiH;

    @FXML
    private TextField txtBrojMjeseci;

    @FXML
    private CheckBox chkDjecaNosilacaRPriznanja;

    @FXML
    private CheckBox chkStudentLogoras;

    @FXML
    private CheckBox chkRoditeljLogoras;

    @FXML
    private CheckBox chkDjecaBez1Roditelja;

    @FXML
    private CheckBox chkDjecaBezObaRoditelja;

    @FXML
    private CheckBox chkBezRoditeljskogStaranja;

    @FXML
    private CheckBox chkKorisniciSocijalnePomoci;

    private int ukupniBodovi;

    private String pdfBase64;

    @FXML
    private Label lblPdf;

    public int getUkupniBodovi() {
        return ukupniBodovi;
    }


    private KriterijPoOsnovuDjeceBranilaca kriterij = new KriterijPoOsnovuDjeceBranilaca();


    public double izracunajBodove() {
        Map<String, Boolean> stanja = new HashMap<>();
        stanja.put("StudentRvi", chkStudentRVI.isSelected());
        stanja.put("Invalidnost", chkInvaliditet.isSelected());
        stanja.put("PoginuoBranilac", chkDjecaSehida.isSelected());
        stanja.put("ClanPorodiceSehida", chkClanPorodiceSehida.isSelected());
        stanja.put("InvalidnostRoditelja", chkInvalidnostRoditelja.isSelected());
        stanja.put("DijeteOSRBiH", chkDjecaOSRBiH.isSelected());
        stanja.put("DjecaNosilacaRPriznanja", chkDjecaNosilacaRPriznanja.isSelected());
        stanja.put("StudentLogoras", chkStudentLogoras.isSelected());
        stanja.put("RoditeljLogoras", chkRoditeljLogoras.isSelected());
        stanja.put("BezJednogRoditelja", chkDjecaBez1Roditelja.isSelected());
        stanja.put("BezObaRoditelja", chkDjecaBezObaRoditelja.isSelected());
        stanja.put("BezRoditeljskogStaratelja", chkBezRoditeljskogStaranja.isSelected());
        stanja.put("KorisniciSocijalnePomoci", chkKorisniciSocijalnePomoci.isSelected());

        double bodovi = kriterij.izracunaj(
                stanja,
                txtPostotakInvaliditeta,
                txtPostInvalidnostiRoditelja,
                txtBrojMjeseci
        );

        this.ukupniBodovi = (int) Math.round(bodovi);
        return this.ukupniBodovi;
    }

    @FXML
    private void onDodajPdf() {
        pdfBase64 = PdfService.uploadPdf(lblPdf.getScene().getWindow());

        if (pdfBase64 != null) {
            lblPdf.setText("PDF dodat");
        } else {
            lblPdf.setText("PDF nije odabran");
        }
    }
}
