package org.example.gestionenergetique.DAO;

import org.example.gestionenergetique.model.Appareil;
import org.example.gestionenergetique.model.Historique;
import org.example.gestionenergetique.utils.PasswordUtils;

import javax.swing.plaf.PanelUI;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL  = "jdbc:sqlite:gestion_energetique.db";

    private Database() {
        try {
            //connection = DriverManager.getConnection(DB_URL, USER, PASS);
            Connection connection = DriverManager.getConnection(URL);
            //connection.close();
            initDB();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    public static void initDB() throws SQLException {
        try(Statement stmt = getConnection().createStatement()){
            stmt.execute("CREATE TABLE IF NOT EXISTS users(" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "nom TEXT NOT NULL, email TEXT NOT NULL UNIQUE," +
                         " mot_de_passe TEXT NOT NULL, " +
                         "role TEXT NOT NULL CHECK(role IN ('admin', 'user')), " +
                         "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            //insertion de l'user admin par defaut
            PreparedStatement ps = getConnection().prepareStatement("INSERT INTO users(nom, email, mot_de_passe, role) VALUES(?,?,?,?)");
            ps.setString(1, "admin");
            ps.setString(2, "admin@gmail.com");
            String mdp = PasswordUtils.hashPassWord("admin123");
            ps.setString(3, mdp);
            ps.setString(4, "admin");
            ps.executeUpdate();

            stmt.execute("CREATE TABLE IF NOT EXISTS appareils(id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT, puissance REAL, actif INTEGER, heure_on TEXT DEFAULT NULL, heure_off TEXT DEFAULT NULL, priorite TEXT DEFAULT 'moyenne', piece TEXT DEFAULT 'Maison', type TEXT DEFAULT 'Lumière')");
            stmt.execute("CREATE TABLE IF NOT EXISTS parametres (cle TEXT PRIMARY KEY, valeur TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS consommations(id INTEGER PRIMARY KEY AUTOINCREMENT,id_appareil INTEGER NOT NULL, nom TEXT DEFAULT NULL, duree INTEGER, consommation REAL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (id_appareil) REFERENCES appareils(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS planifications(id INTEGER PRIMARY KEY AUTOINCREMENT,id_appareil INTEGER NOT NULL, heure_on TEXT, heure_off TEXT, FOREIGN KEY (id_appareil) REFERENCES appareils(id) ON DELETE CASCADE)");
            stmt.execute("CREATE TABLE IF NOT EXISTS historiques(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, total_kwh REAL)");
            stmt.executeUpdate("ALTER TABLE historiques ADD COLUMN appareil TEXT, ADD COLUMN duree REAL, ADD COLUMN consommation REAL");
            getConnection().close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void saveHistorique(double total){
        String date = LocalDate.now().toString();
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO historiques(date, total_kwh) VALUES(?,?)"
        )){
            ps.setString(1, date);
            ps.setDouble(2, total);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static  void save(Appareil a) throws SQLException {
        try(PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO appareils(nom, puissance, actif, priorite) VALUES(?, ?, ?, ?)"))
        {
            ps.setString(1, a.getNom());
            ps.setDouble(2, a.getPuissanceA());
            ps.setInt(3, a.isActif() ? 1 : 0);
            ps.setString(4, a.getPriorite());
            ps.executeUpdate();
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSeuil(double seuil){
        try(PreparedStatement ps = getConnection().prepareStatement(
                "REPLACE INTO parametres(cle, valeur) VALUES('seuil',?)"
        )){
            ps.setString(1, String.valueOf(seuil));
            ps.executeUpdate();
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static double loadSeuil(){
        try (Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT valeur FROM parametres WHERE cle = 'seuil'"))
        {
            if (rs.next()){
                return Double.parseDouble(rs.getString("valeur"));
            }
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 3.0;
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
                    if(rs.getInt("actif") == 1){
                        a.allumer();
                    }
                    list.add(a);
                }
                getConnection().close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return list;
    }

    public static void updateEtat(Appareil a) throws SQLException {
        try(PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE appareils set actif = ?, puissance = ? WHERE nom = ?"
        )){
            ps.setInt(1, a.isActif() ? 1 : 0);
            ps.setDouble(2, a.getPuissanceA());
            ps.setString(3, a.getNom());
            ps.executeUpdate();
            getConnection().close();
        }
    }

    public static void delete(Appareil a) throws SQLException {
        try(PreparedStatement ps = getConnection().prepareStatement(
                "DELETE FROM appareils WHERE nom = ?"
        )){
            ps.setString(1, a.getNom());
            ps.executeUpdate();
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateAppareil(Appareil a) {
        try(PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE appareils set puissance = ?, actif = ? WHERE nom = ?"
        )){
            ps.setDouble(1, a.getPuissance());
            ps.setInt(2, a.isActif() ? 1 : 0);
            ps.setString(3, a.getNom());
            ps.executeUpdate();
            getConnection().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getIdAppareil(String nom){
        String sql = "SELECT id FROM appareils WHERE nom = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)){
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public static List<Historique> getHistorique() {
        List<Historique> list = new ArrayList<>();
        try(Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM historiques")){
            while (rs.next()){
                list.add(new Historique(rs.getString("date"), rs.getDouble("total_kwh")));
            }
            getConnection().close();
        }catch (SQLException e){
            e.printStackTrace();
        }

        return list;
    }
}
