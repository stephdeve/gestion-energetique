package org.example.gestionenergetique.service;

public class PaysInfo {
    public double prixKwh;
    public String devise;
    public String symbole;

    public PaysInfo(double prixKwh, String devise, String symbole){
        this.prixKwh = prixKwh;
        this.devise = devise;
        this.symbole = symbole;
    }
}

