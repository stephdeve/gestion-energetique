package org.example.gestionenergetique.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.gestionenergetique.DAO.UserDAO;
import org.example.gestionenergetique.model.User;
import org.example.gestionenergetique.session.AppSession;
import org.example.gestionenergetique.utils.PasswordUtils;

import java.io.IOException;

public class LoginController {
    @FXML public TextField emailField;
    @FXML public TextField passwordField;
    @FXML public Label errorLabel;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        if (email.isEmpty() || password.isEmpty()){
            errorLabel.setText("Veuillez remplir tous les champs");
            errorLabel.setVisible(true);
            return;
        }
        User user = userDAO.verifierConnexion(email);
        if(user != null){
            if (PasswordUtils.verifyPassword(password, user.getMotDepasse())){
                AppSession.setUserConnecte(user);
                chargerMainView();//On redirige vers l'interface principale
            }else{
                errorLabel.setText("Mot de passe incorrect !");
                errorLabel.setVisible(true);
            }

        }else {
            errorLabel.setText("Utilisateur introuvable !");
            errorLabel.setVisible(true);
        }
    }

    private void chargerMainView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/gestionenergetique/view/dashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleGoToRegister(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/gestionenergetique/view/register-view.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer un compte");
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
