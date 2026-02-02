package org.example.gestionenergetique.model;

public class ConsommationHistorique {
    private final int id;
    private final String appareil;
    private final String date;
    private final double duree;
    private final double consommation;

    public ConsommationHistorique(int id, String appareil, String date, double duree, double consommation) {
        this.id = id;
        this.appareil = appareil;
        this.date = date;
        this.duree = duree;
        this.consommation = consommation;
    }

    public int getId() {
        return id;
    }

    public String getAppareil() {
        return appareil;
    }

    public String getDate() {
        return date;
    }

    public double getDuree() {
        return duree;
    }

    public double getConsommation() {
        return consommation;
    }
}
