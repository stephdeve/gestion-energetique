package org.example.gestionenergetique.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class HistoriqueConsommation {
    private final SimpleStringProperty nomAppareil;
    private final SimpleIntegerProperty duree;
    private final SimpleDoubleProperty consommation;
    private final SimpleStringProperty timestamp;

    public HistoriqueConsommation(String nom, int duree, double consommation, String timestamp){
        this.nomAppareil = new SimpleStringProperty(nom);
        this.duree = new SimpleIntegerProperty(duree);
        this.consommation = new SimpleDoubleProperty(consommation);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public String getNomAppareil(){
        return nomAppareil.get();
    }

    public int getDureeAppareil(){
        return duree.get();
    }

    public double getConsommationAppareil() {
        return consommation.get();
    }

    public String getTimestamp(){
        return timestamp.get();
    }
}
