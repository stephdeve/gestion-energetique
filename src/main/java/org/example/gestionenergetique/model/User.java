package org.example.gestionenergetique.model;

import javax.xml.crypto.dsig.spec.XSLTTransformParameterSpec;

public class User {
    private int id;
    private String nom;
    private String email;
    private String motDepasse;
    private  String role;
    private String createdAt;


    public User() {
    }

    public User(int id, String nom, String email, String motDepasse, String role, String createdAt) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDepasse = motDepasse;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(String nom, String email, String motDepasse, String role) {
        this.nom = nom;
        this.email = email;
        this.motDepasse = motDepasse;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDepasse() {
        return motDepasse;
    }

    public void setMotDepasse(String motDepasse) {
        this.motDepasse = motDepasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    //Les utilitaires
    public boolean estAdmin(){
        return "admin".equalsIgnoreCase(role);
    }

    public boolean estUser(){
        return "user".equalsIgnoreCase(role);
    }
}
