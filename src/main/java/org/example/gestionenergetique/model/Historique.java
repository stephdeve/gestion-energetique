package org.example.gestionenergetique.model;

import javafx.beans.property.SimpleStringProperty;

public class Historique {
    private  final SimpleStringProperty date;
    private final SimpleStringProperty total;

    public Historique(String date, double total){
        this.date = new SimpleStringProperty(date);
        this.total = new SimpleStringProperty(total + " KWh");
    }

    public SimpleStringProperty dateProperty(){ return date; }
    public SimpleStringProperty totalProperty(){ return total; }
}
