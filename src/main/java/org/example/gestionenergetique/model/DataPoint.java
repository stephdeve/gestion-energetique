package org.example.gestionenergetique.model;

public class DataPoint {
    private final String label;
    private final double value;

    public DataPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }

}
