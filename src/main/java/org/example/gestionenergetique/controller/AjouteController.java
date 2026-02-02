package org.example.gestionenergetique.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.gestionenergetique.DAO.Database;
import org.example.gestionenergetique.model.Appareil;

import java.sql.SQLException;

public class AjouteController {
    @FXML public TextField nomField;
    @FXML public TextField puissanceField;
    @FXML public ComboBox<String> prioriteCombo;
    @FXML public ComboBox<String> typeCombo;
    @FXML public ComboBox<String> pieceCombo;

    private MainController mainController;
    public void setMainController(MainController ctrl){
        this.mainController = ctrl;
    }

    @FXML
    public void onAjouter(ActionEvent actionEvent) {
        String nom = nomField.getText().trim();

        if (nom.isEmpty()){
            System.out.println("Veillez entrer le nom de l'appareil");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs requis");
            alert.setHeaderText("Nom de l'appareil manquant");
            alert.setContentText("Veuillez entrer un nom pour l'appareil.");
            alert.showAndWait();
            return;
        }else {
            try {
                double puissance = Double.parseDouble(puissanceField.getText().trim());
                String piece = pieceCombo.getValue().toString();
                String type = typeCombo.getValue().toString();

                Appareil a = new Appareil(nom, puissance);
                a.setPriorite(String.valueOf( prioriteCombo.getValue()));
                synchronized (this){
                    Database.save(a);
                }

                mainController.ajouterAppareil(a);
                ((Stage) nomField.getScene().getWindow()).close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
