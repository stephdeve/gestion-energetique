package org.example.gestionenergetique.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametreDAO {
    public String get(String cle){
        try(PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT valeur FROM parametres WHERE cle = ?"
        )){
            ps.setString(1, cle);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("valeur"): "false";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(String cle, String valeur){
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "REPLACE INTO parametres(cle, valeur) VALUES(?,?)"
        )){
            ps.setString(1, cle);
            ps.setString(2, valeur);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValeur(String cle) {
        try (PreparedStatement ps = Database.getConnection().prepareStatement(
                "SELECT valeur FROM parametres WHERE cle = ?"
        )){
            ps.setString(1, cle);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return rs.getString("valeur");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Valeur par défaut si non configuré
        if ("prix_kwh".equalsIgnoreCase(cle)){
            return "0.15";
        }
        return "";
    }
}
