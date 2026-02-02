package org.example.gestionenergetique.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalTime;

public class Planification {
    private final StringProperty nomAppareil;
    private final ObjectProperty<LocalTime> heureOn;
    private final ObjectProperty<LocalTime> heureOff;


    public Planification(String nomAppareil, LocalTime heureOn, LocalTime heureOff) {
        this.nomAppareil = new SimpleStringProperty(nomAppareil);
        this.heureOn = new SimpleObjectProperty<>(heureOn);
        this.heureOff = new SimpleObjectProperty<>(heureOff);
    }

    public StringProperty nomAppareilProperty(){return nomAppareil;}
    public ObjectProperty<LocalTime> heureOnProperty(){return heureOn;}
    public ObjectProperty<LocalTime> heureOffProperty(){return heureOff;}
}
