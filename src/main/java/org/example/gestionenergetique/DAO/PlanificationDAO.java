package org.example.gestionenergetique.DAO;

import org.example.gestionenergetique.model.Planification;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlanificationDAO {
    public List<Planification> getAll() {
        List<Planification> list = new ArrayList<>();
        String sql = "SELECT a.nom, p.heure_on, p.heure_off FROM planifications p JOIN appareils a ON a.id = p.id_appareil";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
        ){
            while (rs.next()){
                list.add(new Planification(
                        rs.getString("nom"),
                        LocalTime.parse(rs.getString("heure_on")),
                        LocalTime.parse(rs.getString("heure_off"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void insert(int idAppareil, LocalTime heure_on, LocalTime heure_off){
        String sql = "INSERT INTO planifications(id_appareil, heure_on, heure_off) VALUES(?,?,?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
            ps.setInt(1, idAppareil);
            ps.setString(2, heure_on.toString());
            ps.setString(3, heure_off.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByAppareil(int idAppareil){
        try(PreparedStatement ps = Database.getConnection().prepareStatement(
                "DELETE FROM planifications WHERE id_appareil = ?"
        )){
            ps.setInt(1, idAppareil);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
