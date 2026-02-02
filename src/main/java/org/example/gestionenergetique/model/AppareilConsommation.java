package org.example.gestionenergetique.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class AppareilConsommation {
    private final String nom;
    private final double consommation;

    //les propriéte pour afficher les appareils recemment utilisé
    private final SimpleStringProperty nomAppareil;
    private final SimpleDoubleProperty consommationAppareil;
    private final SimpleStringProperty duree;
    private final SimpleStringProperty timestamp;

    public AppareilConsommation(String nom, double consommation){
        this.nom = nom;
        this.consommation = consommation;
        this.nomAppareil = new SimpleStringProperty(nom);
        this.consommationAppareil = new SimpleDoubleProperty(consommation);
        this.duree = new SimpleStringProperty("");
        this.timestamp = new SimpleStringProperty("");
    }

    //Constructeur pour les simpleProperty
    public AppareilConsommation(String nomAppareil, double consommationAppareil, String duree, String timestamp){
        this.nom = nomAppareil;
        this.consommation = consommationAppareil;
        this.nomAppareil = new SimpleStringProperty(nomAppareil);
        this.consommationAppareil = new SimpleDoubleProperty(consommationAppareil);
        this.duree = new SimpleStringProperty(duree);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public String getNom() {
        return nom;
    }

    public double getConsommation() {
        return Math.round(consommation * 100.0) / 100.0;
    }

    public String getNomAppareil() {
        return nomAppareil.get();
    }

    public SimpleStringProperty nomAppareilProperty() {
        return nomAppareil;
    }

    public double getConsommationAppareil() {
        return consommationAppareil.get();
    }

    public SimpleDoubleProperty consommationAppareilProperty() {
        return consommationAppareil;
    }

    public String getDuree() {
        return duree.get();
    }

    public SimpleStringProperty dureeProperty() {
        return duree;
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public SimpleStringProperty timestampProperty() {
        return timestamp;
    }

}
