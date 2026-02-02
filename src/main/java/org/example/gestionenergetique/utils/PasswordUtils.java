package org.example.gestionenergetique.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtils {
    //On va hasher les mots de passe dès les inscriptions
    public static String hashPassWord(String plainPassword){
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    //Methode de verification du mot de passe dès la connexion d'un utilisateur
    public static boolean verifyPassword(String plainPassword, String hashPassword){
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashPassword);
        return result.verified;
    }
}
