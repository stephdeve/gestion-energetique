package org.example.gestionenergetique.service;

import org.example.gestionenergetique.DAO.AppareilDAO;
import org.example.gestionenergetique.DAO.Database;
import org.example.gestionenergetique.model.Appareil;

public class AppareilService {
    private final AppareilDAO appareilDAO = new AppareilDAO();

    public void allumerParNom(String nom) {
        Appareil app = appareilDAO.getParNom(nom);
        if (app != null && !app.isActif()){
            appareilDAO.allumerAppareil((Database.getIdAppareil(nom)));
        }
    }
    public void eteindreParNom(String nom) {
        Appareil app = appareilDAO.getParNom(nom);
        if (app != null && app.isActif()){
            appareilDAO.eteindreAppareil((Database.getIdAppareil(nom)));
        }
    }


}
