package org.example.gestionenergetique.model;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;

public class Appareil {
    private final SimpleStringProperty idAppareil = new SimpleStringProperty();
    private final StringProperty nomAppareil = new SimpleStringProperty();
    private final SimpleBooleanProperty estActif = new SimpleBooleanProperty(false);
    private final DoubleProperty puissanceA = new SimpleDoubleProperty(0);
    private final LongProperty dureeUtilisation = new SimpleLongProperty(0);
    private final DoubleProperty consommationA = new SimpleDoubleProperty(0);

    public long timestampDebut;

    /*Les propriétés primaires */
    private String nom;
    private boolean actif;
    private double puissance; // en KW
    private long duree;
    private String onTime;
    private String offTime;
    private String priorite;
    private LongProperty dureee = new SimpleLongProperty(0);
    private double consommation;
    public double totol = 0;

    public Appareil(String nom, double puissance){
        this.nomAppareil.set(nom);
        this.puissanceA.set(puissance);
        this.nom = nom;
        this.puissance = puissance;
        this.actif = false;
        this.duree = 0;
        this.onTime = "";
        this.offTime = "";
        this.priorite = "";
        this.consommation = 0;
    }




    public String getNom(){
        return nom;
    }

    public  boolean isActif(){
        return actif;
    }

    public double getPuisance() {
        return puissance;
    }

    public long getDuree(){
        return duree;
    }
    public String getHeureOn() {
        return onTime;
    }

    public String getHeureOff() {
        return offTime;
    }
    public String getPriorite(){ return priorite; }
    //public long getDuree(){
      //  return dureee.get();
    //}

    public LongProperty dureeProperty(){
        return dureee;
    }
    public double getConsommation(){
        return actif ? puissance : 0;
    }
    public double getConsommationTotal(){
        return consommation;
    }

    public void allumer(){
        actif = true;
        if (!estActif.get()){
            estActif.set(true);
            timestampDebut = System.currentTimeMillis();
        }
        //Thread thread = new Thread(() ->{
            //while (actif){
                //try {
                    //Thread.sleep(1000);
                   // duree++;
                    //if(duree % 3600 == 0){
                    //    calculerConsommation();
                   // }
                //} catch (InterruptedException e) {
                 //   throw new RuntimeException(e);
                //}
            //}
      //  });
     //   thread.start();
    }


    public void eteindre(){
        actif = false;
        if(estActif.get()){
            long now = System.currentTimeMillis();
            long dureeSec = (now - timestampDebut) / 1000;
            dureeUtilisation.set(dureeUtilisation.get() + dureeSec);
            double consoKWH = (puissanceA.get() * (dureeSec/3600.0)) / 1000;
            consommationA.set(consommationA.get() + consoKWH);
            totol += consommationA.get() + consoKWH;
            setConsommationTotal(totol);
            estActif.set(false);
        }
        calculerConsommation();
    }

    //public Double calculerConsommationTotale(Double total) {
        //consommation += total;
       // duree = 0;
    //}
    private void calculerConsommation() {
        consommation += (puissance * duree / 3600000);
        duree = 0;
    }

    //getters
    public StringProperty idAppareil(){return idAppareil; }
    public StringProperty nomAppareilProperty(){return  nomAppareil;}
    public BooleanProperty estActifProperty(){return estActif;}
    public DoubleProperty puissanceProperty(){return  puissanceA;}
    public LongProperty dureeUtilisationProperty(){return  dureeUtilisation;}
    public DoubleProperty consommationProperty(){return consommationA;}


    //getters property
    public String getIdApareil(){return idAppareil.get();}
    public String getNomApareil(){return nomAppareil.get();}

    public double getPuissanceA() {
        return puissanceA.get();
    }

    public long getDureeUtilisation() {
        return dureeUtilisation.get();
    }

    public double getConsommationA() {
        return consommationA.get();
    }

    public boolean getEstActifA(){
        return estActif.get();
    }

    public void setNom(String newValue) {
        this.nom = newValue;
    }

    public void setPuissance(double newPuissance) {
        this.puissance = newPuissance;
    }

    public void setConsommationTotal(double consommation){
        this.consommation = consommation;
    }

    public double getPuissance() {
        return puissance;
    }

    public void setDuree(long duree){
        this.duree = duree;
    }
    public void setHeurOn(String onTime) {
        this.onTime = onTime;
    }

    public void setheureOff(String offTime) {
        this.offTime = offTime;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }


    //public void setDuree(long duree){
      //  this.dureee.set(duree);
    //}

}
