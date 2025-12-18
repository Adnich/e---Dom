package service;

import dao.VrstaDokumentaDAO;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;


public class KriterijPoOsnovuDjeceBranilaca {
    public double izracunaj(Map<String, Boolean> stanja, TextField postotakInvalidnosti, TextField postInvRoditelja, TextField brojMjeseci){
        double maxBodovi = 0;

        if(stanja.getOrDefault("StudentRvi", false)){
            double postotak = parseDouble(postotakInvalidnosti.getText());
            double bodovi = (postotak < 60) ? 15 : 20;
            maxBodovi = Math.max(maxBodovi, bodovi);
        }

        if(stanja.getOrDefault("DijeteOSRBiH", false)){
            double mjeseci = parseDouble(brojMjeseci.getText());
            double bodovi = mjeseci * 0.3;
            maxBodovi = Math.max(maxBodovi, bodovi);
        }

        if(stanja.getOrDefault("InvalidnostRoditelja", false)){
            double postotak = parseDouble(postInvRoditelja.getText());
            double bodovi = postotak * 0.1;
            maxBodovi = Math.max(maxBodovi, bodovi);
        }

        Map<String, Double> fiksniBodovi = Map.of(
                "Invalidnost", 10.0,
                "PoginuoBranilac", 50.0,
                "ClanPorodiceSehida ", 20.0,
                "DjecaNosilacaRPriznanja", 10.0,
                "StudentLogoras", 10.0,
                "RoditeljLogoras", 5.0,
                "BezJednogRoditelja", 15.0,
                "BezObaRoditelja", 20.0,
                "BezRoditeljskogStaratelja", 20.0,
                "KorisniciSocijalnePomoci", 10.0
        );
        for (String key : fiksniBodovi.keySet()) {
            if(stanja.getOrDefault(key, false)){
                maxBodovi = Math.max(maxBodovi, fiksniBodovi.get(key));
            }
        }

        return maxBodovi;

    }

    private double parseDouble(String s){
        if(s == null || s.trim().isEmpty()) return 0;
        try {
            return Double.parseDouble(s);
        }catch (NumberFormatException e){
            return 0;
        }
    }
}
