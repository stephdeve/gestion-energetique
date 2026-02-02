package org.example.gestionenergetique.controller;

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
import org.example.gestionenergetique.utils.PasswordUtils;

import java.io.IOException;

public class RegisterController {
    @FXML public TextField nomField;
    @FXML public TextField emailField;
    @FXML public TextField passwordField;
    @FXML public TextField confirmPasswordField;
    @FXML public Label errorLabel;
    @FXML public Label successLabel;
    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleRegister(ActionEvent actionEvent) {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        if (nom.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()){
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!password.equals(confirm)){
            showError("Les mot de passe ne correspondesnt pas");
            return;
        }
        if (userDAO.emailExiste(email)){
            showError("Cet email est déjà utilisé. ");
            return;
        }

        String hash = PasswordUtils.hashPassWord(password);
        User user = new User(nom, email, hash, "user");
        if (userDAO.addUser(user)){
            successLabel.setText("Inscription réussie !");
            successLabel.setVisible(true);
            clearFields();
        }else {
            showError("Erreur lors de l'inscription. ");
        }
    }

    private void clearFields() {
        nomField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    @FXML
    public void retourLogin(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/gestionenergetique/view/login-view.fxml"));
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Connexion");
        stage.show();
    }
}
