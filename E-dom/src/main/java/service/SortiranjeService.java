package service;

import model.Prijava;
import model.Student;

import java.util.List;
import java.util.Map;

public class SortiranjeService<T> {

    public enum SortKriterij {
        NAJNOVIJI, NAJSTARIJI, ID, GODINA_FAKULTETA, PROSJEK
    }

    public List<T> sortiraj(List<T> lista, Map<Integer, ?> studentMap, SortKriterij kriterij) {
        return lista.stream()
                .sorted((o1, o2) -> {
                    if (o1 instanceof Prijava p1 && o2 instanceof Prijava p2) {
                        Student s1 = (Student) studentMap.get(p1.getIdStudent());
                        Student s2 = (Student) studentMap.get(p2.getIdStudent());
                        if (s1 == null || s2 == null) return 0;

                        return switch (kriterij) {
                            case NAJNOVIJI -> Integer.compare(p2.getIdPrijava(), p1.getIdPrijava());
                            case NAJSTARIJI -> Integer.compare(p1.getIdPrijava(), p2.getIdPrijava());
                            case ID -> Integer.compare(p1.getIdPrijava(), p2.getIdPrijava());
                            case GODINA_FAKULTETA -> Integer.compare(s1.getGodinaStudija(), s2.getGodinaStudija());
                            case PROSJEK -> Double.compare(s2.getProsjek(), s1.getProsjek());
                        };
                    } else if (o1 instanceof Student s1 && o2 instanceof Student s2) {
                        return switch (kriterij) {
                            case NAJNOVIJI -> Integer.compare(s2.getIdStudent(), s1.getIdStudent());
                            case NAJSTARIJI -> Integer.compare(s1.getIdStudent(), s2.getIdStudent());
                            case GODINA_FAKULTETA -> Integer.compare(s1.getGodinaStudija(), s2.getGodinaStudija());
                            case PROSJEK -> Double.compare(s2.getProsjek(), s1.getProsjek());
                            default -> 0;
                        };
                    } else return 0;
                })
                .toList();
    }
}
