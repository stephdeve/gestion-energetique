package org.example.gestionenergetique.session;

import org.example.gestionenergetique.model.User;

public class AppSession {
    private static User userConnecte;
    public static void setUserConnecte(User user){
        AppSession.userConnecte = user;
    }

    public static User getUserConnecte(){
        return userConnecte;
    }

    public static void logout(){
        userConnecte = null;
    }

    public static boolean estConnecte(){
        return userConnecte != null;
    }

    public static boolean estAdmin(){
        return estConnecte() && userConnecte.getRole().equalsIgnoreCase("admin");
    }

    public static boolean estUser(){
        return estConnecte() && userConnecte.getRole().equalsIgnoreCase("user");
    }
}
