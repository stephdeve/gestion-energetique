package org.example.gestionenergetique.DAO;

import org.example.gestionenergetique.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public boolean addUser(User user){
        String sql = "INSERT INTO users(nom, email, mot_de_passe, role) VALUES(?,?,?,?)";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
            ps.setString(1, user.getNom());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getMotDepasse());
            ps.setString(4, user.getRole());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean emailExiste(String email){
        String sql = "SELECT id FROM users WHERE email = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User verifierConnexion(String email){
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role"),
                        rs.getString("created_at")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
