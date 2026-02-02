package org.example.gestionenergetique.DAO;

import org.example.gestionenergetique.model.Appareil;
import org.example.gestionenergetique.model.AppareilConsommation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ConsommationDAO {
   public void enregistrer(int idAppareil, int duree, double consommation) throws SQLException {
       PreparedStatement ps = Database.getConnection().prepareStatement(
               "INSERT INTO consommations(id_appareil, duree, consommation) VALUES(?,?,?)"
       );
       ps.setInt(1, idAppareil);
       ps.setLong(2, duree);
       ps.setDouble(3, consommation);
       //ps.setDouble(3, (a.getPuissanceA()*1.0) / 1000.0);
       ps.executeUpdate();
       Database.getConnection().close();
   }

   public Map<String, Double> getConsommationParAppareilSurPeriode(LocalDateTime debut, LocalDateTime fin) throws SQLException {
       Map<String, Double> map = new HashMap<>();
       String sql = """
               SELECT a.nom, SUM(c.consommation) AS total
               FROM consommations c
               JOIN appareils a ON a.id = c.id_appareil
               WHERE c.timestamp BETWEEN ? AND ?
               GROUP BY c.id_appareil
               ORDER BY total DESC
               """;
       try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
           ps.setString(1, debut.toString());
           ps.setString(2, fin.toString());
           ResultSet rs = ps.executeQuery();
           while(rs.next()){
               map.put(rs.getString("nom"),
               rs.getDouble("total"));
           }
       }
       return map;
   }

   public double getConsommationTotalSurPeriode(LocalDateTime debut, LocalDateTime fin){
       String sql = """
               SELECT SUM(consommation)
               FROM consommations
               WHERE timestamp BETWEEN ?
               AND ?
               """;
       try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
           ps.setString(1, debut.toString());
           ps.setString(2, fin.toString());
           ResultSet rs = ps.executeQuery();
           return rs.next() ? rs.getDouble(1): 0;
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
   }

    public double getConsommationTotal(LocalDate debut, LocalDate fin) {
       String sql = "SELECT SUM(consommation) FROM consommations WHERE date(timestamp) BETWEEN ? AND ?";
       double total = 0.0;
       try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
           ps.setString(1, debut.toString());
           ps.setString(2, fin.toString());

           ResultSet  rs = ps.executeQuery();
           if (rs.next()){
               total  =  rs.getDouble(1);
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
       return total;
    }

    public List<AppareilConsommation> getTopConsommateurs(int limit) {
       List<AppareilConsommation> list = new ArrayList<>();
       String sql = "SELECT appareils.nom, SUM(consommation) AS total FROM consommations " +
               "JOIN appareils ON appareils.id = consommations.id_appareil" +
               " GROUP BY id_appareil" +
               " ORDER BY total DESC LIMIT ?";

       try(PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
           ps.setInt(1, limit);
           ResultSet rs = ps.executeQuery();
           while (rs.next()){
               list.add(new AppareilConsommation(rs.getString("nom"), rs.getDouble("total")));
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
       return list;
    }

    public Map<String, Double> getConsoParHeure() {
       Map<String, Double> map = new LinkedHashMap<>();
       String sql = "SELECT strftime('%H', timestamp) AS heure, SUM(consommation) AS total FROM consommations GROUP BY heure ORDER BY heure";

       try(PreparedStatement ps = Database.getConnection().prepareStatement(sql);
           ResultSet rs = ps.executeQuery()){
           while (rs.next()){
               map.put(rs.getString("heure") + "h", rs.getDouble("total"));
           }
       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
        return map;
    }

    public double getTotalToDay() throws SQLException {
       double totalToDay = 0;
       String sql = "SELECT SUM(consommation) AS total FROM consommations WHERE date(timestamp) = date('now')";
       try(PreparedStatement ps = Database.getConnection().prepareStatement(sql);
           ResultSet rs = ps.executeQuery();
       ){
           if (rs.next()){
               totalToDay = rs.getDouble("total");
           }
       }
       return totalToDay;
    }

    public double getTotalYesterDay() throws SQLException {
        double totalYesterDay = 0;
        String sql = "SELECT SUM(consommation) AS total FROM consommations WHERE date(timestamp) = date('now', '-1 day')";
        try(PreparedStatement ps = Database.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ){
            if (rs.next()){
                totalYesterDay = rs.getDouble("total");
            }
        }
        return totalYesterDay;
    }
}
