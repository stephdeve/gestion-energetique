package org.example.gestionenergetique.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.example.gestionenergetique.DAO.AppareilDAO;
import org.example.gestionenergetique.DAO.Database;
import org.example.gestionenergetique.DAO.ParametreDAO;
import org.example.gestionenergetique.DAO.PlanificationDAO;
import org.example.gestionenergetique.model.Appareil;
import org.example.gestionenergetique.model.Planification;
import org.example.gestionenergetique.service.AppareilService;
import org.example.gestionenergetique.service.TemperatureSensorService;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OptimisationController {
    @FXML public TableView<Planification> tablePlanification;
    @FXML public TableColumn<Planification, String> colNomPlanif;
    @FXML public TableColumn<Planification, String> colHeureOn;
    @FXML public TableColumn<Planification, String> colHeureOff;
    @FXML public TableColumn<Planification, String> colActionPlanif;
    @FXML public CheckBox inactiviteCheckbox;
    @FXML public TableView<Appareil> tablePriorite;
    @FXML public TableColumn<Appareil, String> colNomPriorite;
    @FXML public TableColumn<Appareil, String> colPrioriteCombo;
    @FXML public Label temperatureLabel;
    @FXML public CheckBox climAutoCheckbox;
    @FXML public CheckBox autoShutdownCheckbox;
    // Contrôles Arduino
    @FXML public ComboBox<String> portCombo;
    @FXML public Button refreshPortsButton;
    @FXML public Button connectButton;
    @FXML public Label portStatusLabel;

    @FXML public ComboBox<String> comboAppareilPlanif;
    @FXML public TextField heureOnField;
    @FXML public TextField heureOffField;

    private final TemperatureSensorService temperatureSensorService = new TemperatureSensorService();
    private final AppareilDAO appareilDAO = new AppareilDAO();
    private final ParametreDAO parametreDAO = new ParametreDAO();
    private final AppareilService appareilService = new AppareilService();
    private final PlanificationDAO planificationDAO = new PlanificationDAO();



    public void initialize() throws SQLException {
        List<Appareil> appareils = AppareilDAO.getAll();
        for (Appareil a : appareils){
            comboAppareilPlanif.getItems().add(a.getNomApareil());
        }
        // Le listener sera démarré après connexion
        setupCheckboxes();
        loadAppareil();
        loadPlanifications();
        startPlanificationScheduler();
        initSerialPortControls();
    }
    //Capteur Temperature
    private void setupTemperatureListener(){
        // Démarre l'écoute si déjà connecté
        temperatureSensorService.startListening(temp -> {
            Platform.runLater(() -> {
                temperatureLabel.setText(temp + " °C");
                if (climAutoCheckbox.isSelected()){
                    if (temp >= 28){
                        appareilService.allumerParNom("Climatiseur");
                    } else if (temp <= 23) {
                        appareilService.eteindreParNom("Climatiseur");
                    }
                }
            });
        });
    }

    private void initSerialPortControls(){
        // Remplir la liste des ports
        refreshPortList();
        if (refreshPortsButton != null){
            refreshPortsButton.setOnAction(e -> refreshPortList());
        }
        if (connectButton != null){
            connectButton.setOnAction(e -> onToggleConnection());
        }
        updatePortStatus();
    }

    private void refreshPortList(){
        try {
            String[] ports = temperatureSensorService.listAvailablePorts();
            portCombo.getItems().setAll(ports);
            if (!portCombo.getItems().isEmpty() && portCombo.getValue() == null){
                portCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception ex){
            System.err.println("Erreur lors du listage des ports: " + ex.getMessage());
        }
    }

    private void onToggleConnection(){
        if (temperatureSensorService.isConnected()){
            temperatureSensorService.disconnect();
            updatePortStatus();
            connectButton.setText("Connecter");
        } else {
            String selected = portCombo.getValue();
            if (selected == null || selected.isBlank()){
                showError("Aucun port sélectionné", "Veuillez sélectionner un port COM disponible.");
                return;
            }
            // Extraire le nom système (ex: "COM3 - USB-SERIAL" => "COM3")
            String systemName = selected.split(" - ")[0].trim();
            boolean ok = temperatureSensorService.connectBySystemName(systemName, 9600);
            if (!ok){
                showError("Connexion échouée", "Impossible d'ouvrir le port " + systemName + ". Vérifiez les droits et qu'il n'est pas utilisé.");
                return;
            }
            setupTemperatureListener();
            updatePortStatus();
            connectButton.setText("Déconnecter");
        }
    }

    private void updatePortStatus(){
        boolean connected = temperatureSensorService.isConnected();
        if (portStatusLabel != null){
            portStatusLabel.setText(connected ? "Connecté" : "Déconnecté");
            portStatusLabel.setStyle(connected ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
    }

    private void showError(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Checkboxes: sauvegardes des préférences utilisateurs

    private void setupCheckboxes(){
        inactiviteCheckbox.setSelected(Boolean.parseBoolean(parametreDAO.get("detect_inactivite")));

        climAutoCheckbox.setSelected(Boolean.parseBoolean(parametreDAO.get("auto_clim")));
        autoShutdownCheckbox.setSelected(Boolean.parseBoolean(parametreDAO.get("auto_shutdown")));

        inactiviteCheckbox.setOnAction(e -> parametreDAO.set("detect_inactivite", String.valueOf(inactiviteCheckbox.isSelected())));
        // BUGFIX: sauvegarder la valeur de climAutoCheckbox et non autoShutdownCheckbox
        climAutoCheckbox.setOnAction(e -> parametreDAO.set("auto_clim", String.valueOf(climAutoCheckbox.isSelected())));
        autoShutdownCheckbox.setOnAction(e -> parametreDAO.set("auto_shutdown", String.valueOf(autoShutdownCheckbox.isSelected())));
    }

    //Priorité energetique
    private  void loadAppareil() throws SQLException {
        ObservableList<Appareil> appareils = FXCollections.observableArrayList(AppareilDAO.getAll());

        colNomPriorite.setCellValueFactory(cell -> cell.getValue().nomAppareilProperty());
        colPrioriteCombo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPriorite()));
        colPrioriteCombo.setCellFactory(ComboBoxTableCell.forTableColumn(
                new StringConverter<>() {
                    @Override public String toString(String value){
                        return value;
                    }
                    @Override public String fromString(String string){
                        return string;
                    }
                },
                "haute", "moyenne", "faible"
        ));

        colPrioriteCombo.setOnEditCommit(event -> {
            Appareil appareil = event.getRowValue();
            appareil.setPriorite(event.getNewValue());
            appareilDAO.updatePriorite(Database.getIdAppareil(appareil.getNomApareil()), event.getNewValue());
        });


        tablePriorite.setItems(appareils);
        tablePriorite.setEditable(true);
    }

    //Planification automatique
    private void loadPlanifications(){
        ObservableList<Planification> planifications = FXCollections.observableArrayList(planificationDAO.getAll());
        colNomPlanif.setCellValueFactory(data -> data.getValue().nomAppareilProperty());
        colHeureOn.setCellValueFactory(data -> data.getValue().heureOnProperty().asString());
        colHeureOff.setCellValueFactory(data -> data.getValue().heureOffProperty().asString());

        colActionPlanif.setCellFactory(new Callback<TableColumn<Planification, String>, TableCell<Planification, String>>() {
            @Override
            public TableCell<Planification, String> call(TableColumn<Planification, String> planificationStringTableColumn) {
               return new TableCell<Planification, String>(){
                    final Button btnSuppr = new Button("Supprimer");
                    @Override
                    protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);
                        if (empty){
                            setGraphic(null);
                        }else {
                            btnSuppr.setOnAction(e -> {
                                Planification planif = getTableView().getItems().get(getIndex());
                                planificationDAO.deleteByAppareil(Database.getIdAppareil(planif.nomAppareilProperty().getValue()));
                                chargerPlanifications();
                                System.out.println(planif.nomAppareilProperty().getValue());
                                tablePlanification.getItems().remove(planif);

                            });
                            setGraphic(btnSuppr);
                        }
                    }
                };
            }
        }

                );
        //System.out.println(planifications);
        tablePlanification.setItems(planifications);
    }

    private void chargerPlanifications(){
        List<Planification>  planificationList = planificationDAO.getAll();
        tablePlanification.getItems().setAll(planificationList);
    }

    // Scheduler: vérifie périodiquement l'heure pour appliquer les planifications
    private void startPlanificationScheduler(){
        // Exécuter toutes les 30 secondes pour capturer le changement de minute
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(30), e -> {
                    try {
                        List<Planification> plans = planificationDAO.getAll();
                        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                        for (Planification p : plans){
                            LocalTime on = p.heureOnProperty().get();
                            LocalTime off = p.heureOffProperty().get();
                            if (on != null && now.equals(on)){
                                appareilService.allumerParNom(p.nomAppareilProperty().get());
                            }
                            if (off != null && now.equals(off)){
                                appareilService.eteindreParNom(p.nomAppareilProperty().get());
                            }
                        }
                    } catch (Exception ex){
                        // Logging basique; éviter de bloquer l'UI
                        System.err.println("Erreur scheduler planification: " + ex.getMessage());
                    }
                })
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void onAjouterPlannifierHeure(ActionEvent actionEvent) {
        String nom = comboAppareilPlanif.getValue();
        // Validation des entrées horaires au format HH:mm
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime heureOn = null;
        LocalTime heureOff = null;
        try {
            if (heureOnField.getText() != null && !heureOnField.getText().isBlank()){
                heureOn = LocalTime.parse(heureOnField.getText().trim(), formatter);
            }
            if (heureOffField.getText() != null && !heureOffField.getText().isBlank()){
                heureOff = LocalTime.parse(heureOffField.getText().trim(), formatter);
            }
        } catch (DateTimeParseException ex){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Format d'heure invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez saisir les heures au format HH:mm (ex: 08:30)");
            alert.showAndWait();
            return;
        }

        if (nom == null || heureOn == null || heureOff == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs");
            alert.showAndWait();
            return;
        }

        Appareil appareil = appareilDAO.getParNom(nom);
        if (appareil == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Aucun appareil trouvé");
            alert.showAndWait();
            return;
        }

        planificationDAO.insert(Database.getIdAppareil(nom), heureOn, heureOff);
        chargerPlanifications();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucess");
        alert.setHeaderText(null);
        alert.setContentText("Planification ajoutée avec succès");
        alert.showAndWait();
        return;
    }
}
