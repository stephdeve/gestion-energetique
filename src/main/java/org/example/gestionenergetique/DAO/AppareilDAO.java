package org.example.gestionenergetique.DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.example.gestionenergetique.model.Appareil;
import org.example.gestionenergetique.model.AppareilConsommation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.example.gestionenergetique.DAO.Database.getConnection;

public class    AppareilDAO {
    public void mettreAJourHeuresPlannification(Appareil appareil) {
        String sql = "UPDATE appareils SET heure_on = ?, heure_off = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)){
            ps.setString(1, appareil.getHeureOn());
            ps.setString(2, appareil.getHeureOff());
            ps.setInt(3, Database.getIdAppareil(appareil.getNomApareil()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static List<Appareil> getAll() throws SQLException {
        List<Appareil> list = new ArrayList<>();
        try(Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM appareils")){
            while (rs.next()){
                Appareil a = new Appareil(
                        rs.getString("nom"),
                        rs.getDouble("puissance")
                );
                // Charger l'état actif depuis la DB
                if(rs.getInt("actif") == 1){
                    a.allumer();
                }
                // Charger la priorité depuis la DB si disponible
                try {
                    String priorite = rs.getString("priorite");
                    if (priorite != null) {
                        a.setPriorite(priorite);
                    }
                } catch (SQLException ignored) {}
                list.add(a);
            }
            getConnection().close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return list;
    }

    public Appareil getParNom(String nom){
        try(PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM appareils WHERE nom = ?")) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                Appareil a = new Appareil(
                        rs.getString("nom"),
                        rs.getDouble("puissance")
                );
                // Charger l'état actif et la priorité
                if (rs.getInt("actif") == 1) {
                    a.allumer();
                }
                try {
                    String priorite = rs.getString("priorite");
                    if (priorite != null) {
                        a.setPriorite(priorite);
                    }
                } catch (SQLException ignored) {}
                return a;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    return null;
    }

    public void allumerAppareil(int id){
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE appareils SET actif = 1 WHERE id = ?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void eteindreAppareil(int id) {
        try (PreparedStatement ps = getConnection().prepareStatement("UPDATE appareils SET actif = 0 WHERE id = ?")){
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePriorite(int idAppareil, String newValue) {
            String sql = "UPDATE appareils SET priorite = ? WHERE id = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)){
                ps.setString(1, newValue);
                ps.setInt(2, idAppareil);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    public Pair<Integer , Integer> getNbAppareilActifsEtTotal(){
        int actifs = 0;
        int total = 0;
        String sql = "SELECT SUM(CASE WHEN actif = 1 THEN 1 ELSE 0 END) AS actifs, COUNT(*) AS total FROM appareils";
        try(Statement stmt = Database.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ){
            if (rs.next()){
                actifs = rs.getInt("actifs");
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Pair<>(actifs, total);
    }

    public ObservableList<AppareilConsommation> chargerDonneesConsommation() throws SQLException {
        ObservableList<AppareilConsommation> list = FXCollections.observableArrayList();
        String sql = "SELECT a.nom AS nom_appareil, c.consommation, c.duree, c.timestamp FROM consommations c JOIN appareils a ON a.id = c.id_appareil WHERE c.timestamp >= datetime('now', '-1 day') ORDER BY c.timestamp DESC";
        try(PreparedStatement ps = Database.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ){
            while(rs.next()){
                String nom = rs.getString("nom_appareil");
                double conso = rs.getDouble("consommation");
                String duree = rs.getString("duree");
                String timestamp = rs.getString("timestamp");
                list.add(new AppareilConsommation(nom, conso, duree, timestamp));

            }
        }
        return list;
    }
}
