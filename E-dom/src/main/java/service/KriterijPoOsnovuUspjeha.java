package service;

import dao.PrijavaDAO;

public class KriterijPoOsnovuUspjeha {

    public void izracunajBodove(int prijavaId, double prosjek, int polozeniIspiti, int godinaStudija){
        double bodovi = prosjek*3.5;
        System.out.println("bodovi za kriterij po osnovu uspjeha prije ispita: " + bodovi);
        bodovi += polozeniIspiti*1.8;
        System.out.println("bodovi za kriterij po osnovu uspjeha nakon ispita: " + bodovi);
        switch(godinaStudija){
            case 2:
                bodovi += 3;
                break;
            case 3:
                bodovi += 5;
                break;
            case 4:
                bodovi += 7;
                break;
            case 5:
                bodovi += 9;
                break;
            case 6:
                bodovi += 10;
                break;
            case 7:
                bodovi += 12; //apsolventi
                break;
            case 8:
                bodovi += 10; // postdiplomci
                break;
        }
        System.out.println("bodovi za kriterij po osnovu uspjeha nakon godine studija: " + bodovi);
        PrijavaDAO pDAO = new PrijavaDAO();
        System.out.println("bodovi za kriterij po osnovu uspjeha: " + bodovi);
        pDAO.dodajBodoveNaPrijavu(prijavaId, bodovi);
    }

    public void izracunajBodoveBrucosi(int prijavaId, double prosjek){
        double bodovi = prosjek*9;
        bodovi += 1;
        PrijavaDAO pDAO = new PrijavaDAO();
        System.out.println("bodovi za kriterij po osnovu uspjeha brucoši: " + bodovi);
        pDAO.dodajBodoveNaPrijavu(prijavaId, bodovi);
    }

}
