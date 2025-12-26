package service;

import model.Prijava;
import model.Student;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FiltriranjeService<T> {

    public List<T> filtriraj(
            List<T> lista,
            Map<Integer, Student> studentMap,
            Set<String> fakulteti,
            Set<Integer> godineFakulteta,
            Set<String> socijalniStatusi,
            Set<String> statusPrijave
    ) {
        return lista.stream()
                .filter(o -> {
                    Student s = null;
                    String status = null;

                    if (o instanceof Prijava p) {
                        s = studentMap.get(p.getIdStudent());
                        status = p.getStatusPrijave() != null ? p.getStatusPrijave().getNaziv() : "";
                    } else if (o instanceof Student st) {
                        s = st;
                    }

                    if (s == null) return false;

                    boolean ok = true;
                    if (!fakulteti.isEmpty()) ok &= fakulteti.contains(s.getFakultet());
                    if (!godineFakulteta.isEmpty()) ok &= godineFakulteta.contains(s.getGodinaStudija());
                    if (!socijalniStatusi.isEmpty()) ok &= socijalniStatusi.contains(s.getSocijalniStatus());
                    if (!statusPrijave.isEmpty() && status != null) ok &= statusPrijave.contains(status);

                    return ok;
                })
                .toList();
    }
}
