package util;

import java.util.List;
import java.util.Map;
import dao.DokumentDAO;
import dao.StudentDAO;
import java.util.HashMap;
import model.Dokument;
import model.Prijava;
import model.Student;

public class BodoviUtil {

    public static Map<String, Double> izracunajBodoveZaPrijavu(Prijava p) {
        List<Dokument> dokumenti = new DokumentDAO().dohvatiDokumenteZaPrijavu(p.getIdPrijava());
        Student student = new StudentDAO().dohvatiStudentaPoId(p.getIdStudent());

        double godinaBodovi = switch (student.getGodinaStudija()) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 7;
            case 5 -> 9;
            case 6, 8 -> 10;
            case 7 -> 12;
            default -> 0;
        };

        double bodoviUspjeh = dokumenti.stream()
                .filter(d -> d.getVrstaDokumenta() != null &&
                        (d.getVrstaDokumenta().getNaziv().toLowerCase().contains("svjed") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("indeks") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("uvjerenje o položenim ispitima")))
                .mapToDouble(Dokument::getBrojBodova)
                .sum() - godinaBodovi;

        double nagradeBodovi = dokumenti.stream()
                .filter(d -> d.getVrstaDokumenta() != null && d.getVrstaDokumenta().getNaziv().toLowerCase().contains("nagrad"))
                .mapToDouble(Dokument::getBrojBodova)
                .sum();

        double socijalniBodovi = dokumenti.stream()
                .filter(d -> d.getVrstaDokumenta() != null && d.getVrstaDokumenta().getNaziv().toLowerCase().contains("domać"))
                .mapToDouble(Dokument::getBrojBodova)
                .sum();

        double udaljenostBodovi = dokumenti.stream()
                .filter(d -> d.getVrstaDokumenta() != null && d.getVrstaDokumenta().getNaziv().toLowerCase().contains("cips"))
                .mapToDouble(Dokument::getBrojBodova)
                .sum();

        double dodatniBodovi = dokumenti.stream()
                .filter(d -> d.getVrstaDokumenta() != null &&
                        (d.getVrstaDokumenta().getNaziv().toLowerCase().contains("invalidnost") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("izbjegličkom") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("logoraš") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("poginulim") ||
                                d.getVrstaDokumenta().getNaziv().toLowerCase().contains("učešću")))
                .mapToDouble(Dokument::getBrojBodova)
                .sum();

        Map<String, Double> bodoviMap = new HashMap<>();
        bodoviMap.put("uspjeh", bodoviUspjeh);
        bodoviMap.put("nagrade", nagradeBodovi);
        bodoviMap.put("socijalni", socijalniBodovi);
        bodoviMap.put("udaljenost", udaljenostBodovi);
        bodoviMap.put("dodatni", dodatniBodovi);
        bodoviMap.put("godina", godinaBodovi);
        bodoviMap.put("ukupno", p.getUkupniBodovi());
        return bodoviMap;
    }
}
