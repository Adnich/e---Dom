package service;

import dao.PrijavaDAO;

public class KriterijPoOsnovuUspjeha {

    public void izracunajBodove(int prijavaId, double prosjek, int polozeniIspiti, int godinaStudija){
        double bodovi = prosjek*3.5;
        bodovi += polozeniIspiti*1.8;
        switch(godinaStudija){
            case 2:
                bodovi += 3;
            case 3:
                bodovi += 5;
            case 4:
                bodovi += 7;
            case 5:
                bodovi += 9;
            case 6:
                bodovi += 10;
            case 7:
                bodovi += 12;
            case 8:
                bodovi += 10;
        }
        PrijavaDAO pDAO = new PrijavaDAO();
        pDAO.dodajBodoveNaPrijavu(prijavaId, bodovi);
    }

    public void izracunajBodoveBrucosi(int prijavaId, double prosjek){
        double bodovi = prosjek*9;
        bodovi += 1;
        PrijavaDAO pDAO = new PrijavaDAO();
        pDAO.dodajBodoveNaPrijavu(prijavaId, bodovi);
    }
}
