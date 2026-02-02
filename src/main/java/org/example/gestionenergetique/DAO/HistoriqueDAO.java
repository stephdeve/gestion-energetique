package org.example.gestionenergetique.DAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.gestionenergetique.model.ConsommationHistorique;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HistoriqueDAO {
    public ObservableList<ConsommationHistorique> LoadHistorique(){
        ObservableList<ConsommationHistorique> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM historiques ORDER BY date DESC";
        try{
            Statement stmt = Database.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                list.add(
                        new ConsommationHistorique(
                                rs.getInt("id"),
                                rs.getString("appareil"),
                                rs.getString("date"),
                                rs.getDouble("duree"),
                                rs.getDouble("consommation")
                        )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
