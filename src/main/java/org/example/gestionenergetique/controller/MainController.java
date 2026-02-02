package org.example.gestionenergetique.controller;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.gestionenergetique.DAO.AppareilDAO;
import org.example.gestionenergetique.DAO.ConsommationDAO;
import org.example.gestionenergetique.DAO.Database;
import org.example.gestionenergetique.model.Appareil;
import org.example.gestionenergetique.model.HistoriqueConsommation;
import org.example.gestionenergetique.model.User;
import org.example.gestionenergetique.service.PaysInfo;
import org.example.gestionenergetique.session.AppSession;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MainController {
    @FXML public TableView<Appareil> tableAppareils;
    @FXML public TableColumn<Appareil, String> colNom;
    @FXML public TableColumn<Appareil, String> colEtat;
    @FXML public TableColumn<Appareil, String> colPuissance;
    @FXML public TableColumn<Appareil, Number> colDuree;
    @FXML public   TableColumn<Appareil,String> colConso;
    @FXML public Label savingsLabel;
    @FXML public Label labelAlert;
    @FXML public Label totalConsommationLabel;
    @FXML public LineChart<String, Number> dailyChart;
    @FXML public BarChart<String, Number> monthlyChart;
    @FXML public LineChart<String, Number> annualyChart;
    @FXML public DatePicker datePicker;
    @FXML public ComboBox<String> comboTypeVue;
    @FXML public TableView<HistoriqueConsommation> tableHistorique;
    @FXML public TableColumn<HistoriqueConsommation, String> colNomAppareil;
    @FXML public TableColumn<HistoriqueConsommation, Integer> colDureeAppareil;
    @FXML public TableColumn<HistoriqueConsommation, Double> colConsoAppareil;
    @FXML public TableColumn<HistoriqueConsommation, String> colDateAppareil;
    @FXML public LineChart<String, Number> chartHistorique;
    @FXML public Label nomUserlabel;
    @FXML public Label roleUserLabel;
    @FXML public Label etatLabel;
    @FXML public TextField heureOnField;
    @FXML public TextField heureOffField;

    @FXML public ListView<String> listeComparative;
    @FXML public TextField prixKwhField;
    @FXML public Label resultatFactureLabel;
    @FXML public Button deconnexion;
    @FXML public Tab tabFacture;

    @FXML public FactureController factureController;
    @FXML public ComboBox<String> paysCombo;

    private double seuilkw = 3.0;
    @FXML public TextField seuilField;
    private double consommationTotal;
    private ConsommationDAO consommationDAO;
    private boolean seuilDejaDepasse = false;
    private  final Map<Integer, LocalDateTime> heureAllumageMap = new HashMap<>();
    private AppareilDAO appareilDAO;

    private Map<String, Double> prixKwhParPays = new HashMap<>();
    private Map<String, PaysInfo> paysInfos = new HashMap<>();

    private ObservableList<Appareil> appareils = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        for(MaterialDesignIcon icon : MaterialDesignIcon.values()){
            System.out.println(icon.name());
        }
        Database.initDB();
        if (AppSession.estConnecte()){
            User user= AppSession.getUserConnecte();
            nomUserlabel.setText(user.getNom());
            roleUserLabel.setText(user.getRole().toUpperCase());
            etatLabel.setText(user.getNom()+ " Connecté");
            deconnexion.setVisible(true);
        }
            //System.out.println("Acces refusé: Aucun utilisateur connecté");
           // getLoginView();
           // return;
       // }
        seuilkw = Database.loadSeuil();
        seuilField.setText(String.valueOf(seuilkw));
        //Initialisation des colonnes
        colNom.setCellValueFactory(data-> new SimpleStringProperty(data.getValue().getNom()));
        colEtat.setCellValueFactory(data-> new SimpleStringProperty(data.getValue().isActif() ? "Allumer" : "Eteint"));
        colPuissance.setCellValueFactory(data-> new SimpleStringProperty(data.getValue().getPuissanceA() + "W"));
        colDuree.setCellValueFactory(data-> data.getValue().dureeUtilisationProperty());
        colDuree.setCellFactory(new Callback<TableColumn<Appareil, Number>, TableCell<Appareil, Number>>() {
            @Override
            public TableCell<Appareil, Number> call(TableColumn<Appareil, Number> param) {
                return new TableCell<>(){
                    @Override
                    protected void updateItem(Number item, boolean empty){
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null){
                            setText(null);
                        }else {
                            Appareil a = (Appareil) getTableRow().getItem();
                            long totalSec = a.getDureeUtilisation();
                            if (a.estActifProperty().get()){
                                long now = System.currentTimeMillis();
                                totalSec += (now - a.timestampDebut) / 1000;
                            }
                            long h = totalSec/3600, m = (totalSec%3600)/60, s = totalSec % 60;
                            setText(String.format("%02d:%02d:%02d", h, m, s));
                        }
                    }
                };
            }
        });
        colConso.setCellValueFactory(data-> new SimpleStringProperty(data.getValue().getConsommationA() + "KW"));


        tableAppareils.setEditable(true);
        colNom.setCellFactory(TextFieldTableCell.forTableColumn());
        colNom.setOnEditCommit(e ->{
            Appareil a = e.getRowValue();
            a.setNom(e.getNewValue());
            Database.updateAppareil(a);
        });

        colConso.setCellFactory(TextFieldTableCell.forTableColumn());
        colNom.setOnEditCommit(e ->{
            Appareil a = e.getRowValue();
            try {
                double newVal = Double.parseDouble(e.getNewValue().replace(" KW", ""));
                a.setPuissance(newVal);
                Database.updateAppareil(a);
                tableAppareils.refresh();
            }catch (NumberFormatException ex){
                System.out.println("Valeur non valide");
            }
            a.setNom(e.getNewValue());
            Database.updateAppareil(a);
        });

        appareils.addAll(Database.getAll());
        tableAppareils.setItems(appareils);
        try {
            consommationDAO = new ConsommationDAO();
            appareilDAO = new AppareilDAO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //tableau historique
        double total = appareils.stream().filter(Appareil::getEstActifA).mapToDouble(Appareil::getConsommationA).sum();
        System.out.println(total);
        Database.saveHistorique(total);
        //Tableau de l'historique
        colNomAppareil.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomAppareil()));
        colDureeAppareil.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getDureeAppareil()).asObject());
        colConsoAppareil.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getConsommationAppareil()).asObject());
        colDateAppareil.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTimestamp()));

        chargeStateMensuelles();
        System.out.println(Database.getIdAppareil("Frigo") + " test");
        startTimer();
        startUIRefresh();
        mettreAjourDuree();
        planifierCalculConsommationTotale();
        verifierSurConsommation();

        System.out.println(5);
        calculerConsommationTotale();
        initialiserPrixParPays();

        //tabFacture.setOnSelectionChanged(event -> {
          //  if (tabFacture.isSelected()){
            //    factureController.initialiser();
            //}
        //});
    }

    @FXML
    public void onAjouter(ActionEvent actionEvent) throws SQLException {
        verifierSurConsommation();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gestionenergetique/view/ajoute-view.fxml"));
            System.out.println(loader);

            Parent root = loader.load();
            System.out.println(root);
            AjouteController ctrl = loader.getController();
            if (ctrl == null){
                System.out.println("Erreur: le controller est null");
            }else {
                System.out.println(ctrl);
                ctrl.setMainController(this);

                Stage stage = new Stage();
                stage.setTitle("Nouvel appareil");
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Ajouter cliqué");
    }

    @FXML
    public void onAllumer(ActionEvent actionEvent) throws SQLException {
        verifierSurConsommation();
        Appareil selected = tableAppareils.getSelectionModel().getSelectedItem();
        if(selected != null){
            selected.allumer();
            Database.updateEtat(selected);
            tableAppareils.refresh();
            synchronized (this){
                heureAllumageMap.put(Database.getIdAppareil(selected.getNomApareil()), LocalDateTime.now());
            }
            System.out.println("Allumer cliqué" + Database.getIdAppareil(selected.getNomApareil()) + LocalDateTime.now()+ " r "+heureAllumageMap.get(Database.getIdAppareil(selected.getNomApareil())));
        }

    }

    @FXML
    public void onEteindre(ActionEvent actionEvent) throws SQLException {
        verifierSurConsommation();
        double conTotal = calculerConsoTotale();
        verifierSeuil(conTotal);
        Appareil selected = tableAppareils.getSelectionModel().getSelectedItem();
        if(selected != null) {
            selected.eteindre();
            Database.updateEtat(selected);
            tableAppareils.refresh();
            int idAppareil = Database.getIdAppareil(selected.getNomApareil());
            long duree = calculerDureeDepuisAllumage(idAppareil);
            double consommation = calculConsommation(selected.getPuissanceA(), duree);
            synchronized (this){
                int dureeMinute = (int) Math.ceil(duree/60.0);
                System.out.println("Test23 " + idAppareil+ " "+ selected.getPuissanceA() +" "+ duree +" " + consommation);
                consommationDAO.enregistrer(idAppareil, dureeMinute, consommation);
            }
            //heureAllumageMap.remove(Database.getIdAppareil(selected.getNomApareil()));
            System.out.println("Eteindre cliqué");
        }

    }

    public void onModifier(ActionEvent actionEvent) {
    }

    public void onSupprimer(ActionEvent actionEvent) throws SQLException {
        Appareil selected = tableAppareils.getSelectionModel().getSelectedItem();
        if(selected != null) {
            appareils.remove(selected);
            synchronized (this){
                Database.delete(selected);
            }
            tableAppareils.refresh();
            System.out.println("Appareil supprimé");
        }else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aucun appareil sélectionné");
            alert.setHeaderText("Suppression impossible");
            alert.setContentText("Veuillez sélectionner un appareil à supprimer.");
            alert.showAndWait();
            return;
        }
    }

    public void ajouterAppareil(Appareil a) {
        appareils.add(a);
    }

    public void verifierSurConsommation(){
        double total = appareils.stream().mapToDouble(Appareil::getConsommationA).sum();
        if(total > seuilkw){
            labelAlert.setText("Surconsommation : " + total + "KW!");
        }else{
            labelAlert.setText("");
        }
    }

    private void verifierSeuil(double totalConso){
        if(seuilkw > 0 && totalConso >= seuilkw){
            if (!seuilDejaDepasse){
                seuilDejaDepasse = true;
                Platform.runLater(() ->{
                    //          jouerSonAlerte();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Alerte de consommation");
                    alert.setHeaderText("Seuil dépassé !");
                    alert.setContentText("La consommation a ateint "+ totalConso+ " KWH.");
                    alert.showAndWait();
                });
            }

        }else {
            seuilDejaDepasse = false;
        }
    }

    private double calculerConsoTotale(){
        consommationTotal = 0;
        for (Appareil appareil : appareils){
            //consommationTotal += appareil.getConsommationA();
            consommationTotal += appareil.getConsommationTotal();
        }
        return consommationTotal;
    }

    public void initialiserPrixParPays(){
        paysInfos.put("France (0.18 €)", new PaysInfo(0.18, "EUR", "€"));
        paysInfos.put("Allemagne (0.32 €)", new PaysInfo(0.18, "EUR", "€"));
        paysInfos.put("Belgique (0.28 €)", new PaysInfo(0.18, "EUR", "€"));
        paysInfos.put("Espagne (0.24 €)", new PaysInfo(0.18, "EUR", "€"));
        paysInfos.put("Italie (0.26 €)", new PaysInfo(0.18, "EUR", "€"));
        paysInfos.put("Suisse (0.21 €)", new PaysInfo(0.18, "CHF", "CHF"));
        paysInfos.put("Canada (0.12 CAD)", new PaysInfo(0.18, "CAD", "€"));
        paysInfos.put("USA (0.15 $)", new PaysInfo(0.18, "USD", "$"));
        paysInfos.put("Bénin (0.1 FCFA)", new PaysInfo(0.1, "FCFA", "FCFA"));
        paysInfos.put("Sénégal (0.11 FCFA)", new PaysInfo(0.11, "FCFA", "FCFA"));
        paysInfos.put("Côte d'Ivoire (0.12 FCFA)", new PaysInfo(0.12, "FCFA", "FCFA"));
        paysInfos.put("Maroc (0.14 MAD)", new PaysInfo(0.14, "MAD", "MAD"));
        paysInfos.put("Tunisie (0.13 TND)", new PaysInfo(0.13, "TND", "TND"));
        paysInfos.put("Algérie (0.05 DZD)", new PaysInfo(0.05, "DZD", "DZD"));

        //configuration de la combo Pays
        paysCombo.getItems().addAll(paysInfos.keySet());
        //paysCombo.getSelectionModel().selectFirst();

        //Selectionne le premier pays par defaut
        if (!paysInfos.isEmpty()){
            String premierPays = paysInfos.keySet().iterator().next();
            paysCombo.getSelectionModel().select(premierPays);
            PaysInfo prix = paysInfos.get(premierPays);
            getPrice(prix);
        }
        paysCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->{
            if(newVal != null){
                PaysInfo info = paysInfos.get(newVal);
                getPrice(info);
            }
        });
    }

    public void getPrice(PaysInfo info){
        try{
            double totalConso = calculerConsoTotale();
            double prixTotal = totalConso * info.prixKwh;
            prixTotal = Math.round(prixTotal * 100.0) / 100.0;
            double finalPrixTotal = prixTotal;
            String textEconomie;
            if ("FCFA".equals(info.symbole)){
                textEconomie = String.format("%, .3f %s", finalPrixTotal, info.symbole);
            }else{
                textEconomie = String.format("%, .2f %s", finalPrixTotal, info.symbole);
            }
            Platform.runLater(()-> {
                savingsLabel.setText(textEconomie);
            });
        } catch (NumberFormatException e) {
            throw new RuntimeException(e.getMessage());
        }


    }

    private void calculerConsommationTotale(){
        consommationTotal = 0;
        for (Appareil appareil : appareils){
            //consommationTotal += appareil.getConsommationA();
            consommationTotal += appareil.getConsommationTotal();
        }
        double prixKwh = 100;
        double prixTotal = consommationTotal * prixKwh;
        prixTotal = Math.round(prixTotal * 100.0) / 100.0;
        double finalPrixTotal = prixTotal;
        Platform.runLater(()-> {
            totalConsommationLabel.setText(Math.round(consommationTotal * 100.0) / 100.0 + " KWH");
        });
    }


    private void planifierCalculConsommationTotale(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::calculerConsommationTotale, 0, 1, TimeUnit.HOURS);
    }

    private void mettreAjourDuree(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() ->{
            Platform.runLater(() -> {
                for (Appareil appareil : appareils){
                    if (appareil.isActif()){
                        appareil.setDuree(appareil.getDuree() + 1);
                    }
                }
                tableAppareils.setItems(appareils);
            });
                },
                0, 1, TimeUnit.SECONDS);
    }

    public void startTimer(){
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() ->
            Platform.runLater(()-> {
                for (Appareil appareil : appareils){
                    if (appareil.estActifProperty().get()){
                        long elapsed = (System.currentTimeMillis() - appareil.timestampDebut)/1000;
                        if (elapsed >= 3600){
                            appareil.dureeUtilisationProperty().set(appareil.getDureeUtilisation() + 3600);
                            double conso = (appareil.getPuissanceA()*1.0)/1000.0;
                            appareil.consommationProperty().set(appareil.getConsommationA() + conso);
                            calculerConsommationTotale();
                            appareil.timestampDebut = System.currentTimeMillis();
                                try {
                                    long duree = calculerDureeDepuisAllumage(Database.getIdAppareil(appareil.getNomApareil()));
                                    double consommation = calculConsommation(appareil.getPuissanceA(), duree);
                                    synchronized (this){
                                        int idAppareil = Database.getIdAppareil(appareil.getNomApareil());
                                        System.out.println("Test1 " + idAppareil + appareil.getPuissanceA() + consommation);
                                        consommationDAO.enregistrer(idAppareil, 60, consommation);
                                    }

                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                        }
                    }
                    //bloc du code pour verifier l'alluamage ou l'extinction automatique d'un appareil selectionné
                    LocalTime now = LocalTime.now();
                    if (appareil.getHeureOn() != null && now.format(DateTimeFormatter.ofPattern("HH:mm")).equals(appareil.getHeureOn())){
                        appareilDAO.allumerAppareil(Database.getIdAppareil(appareil.getNomApareil()));
                        appareil.allumer();
                        tableAppareils.refresh();
                        System.out.println("On");
                    }
                    System.out.println("Out");
                    if (appareil.getHeureOff() != null && now.format(DateTimeFormatter.ofPattern("HH:mm")).equals(appareil.getHeureOff())){
                        appareilDAO.eteindreAppareil(Database.getIdAppareil(appareil.getNomApareil()));
                        appareil.eteindre();
                        tableAppareils.refresh();
                        System.out.println("Off");
                    }

                    //Controle de seuil et d'extinction par priorité
                    double consoTot = calculerConsoTotale();
                    if (consoTot > seuilkw){
                        try {
                            List<Appareil> allumes = AppareilDAO.getAll().stream().filter(Appareil::getEstActifA).collect(Collectors.toList());
                            allumes.sort(Comparator.comparing(Appareil::getPriorite));
                            for (Appareil app : allumes){
                                appareilDAO.eteindreAppareil(Database.getIdAppareil(app.getNomApareil()));
                                app.eteindre();
                                consoTot -= appareil.getConsommationA();
                                if (consoTot <= seuilkw) break;
                            }
                            //On affiche une alerte ou notification
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                //double conTotal = calculerConsoTotale();
                //verifierSeuil(conTotal);
            }), 0, 1, TimeUnit.SECONDS
        );
    }

    public void startUIRefresh(){
        ScheduledExecutorService uiUpdater = Executors.newScheduledThreadPool(1);
        uiUpdater.scheduleAtFixedRate(()->
                Platform.runLater(()->
                        {
                            tableAppareils.refresh();
                            chargerStatJournalieres();

                            chargeStateAnnuelles();
                            calculerConsommationTotale();
                            double conTotal = calculerConsoTotale();
                            verifierSeuil(conTotal);
                        }),0, 1, TimeUnit.SECONDS);
    }

    public void startUIRefreshCon(){
        ScheduledExecutorService uiUpdater = Executors.newScheduledThreadPool(1);
        uiUpdater.scheduleAtFixedRate(()->
                Platform.runLater(()->
                {
                    tableAppareils.refresh();
                    double conTotal = calculerConsoTotale();
                    verifierSeuil(conTotal);
                }),0, 1, TimeUnit.SECONDS);
    }


    public void onModifierSeuil(ActionEvent actionEvent) {
        try{
            seuilkw = Double.parseDouble(seuilField.getText());
            Database.saveSeuil(seuilkw);
            //jouerSonAlerte();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Seuil défini");
            alert.setHeaderText(null);
            alert.setContentText("Seuil défini à "+ seuilkw+ " KWH.");
            alert.showAndWait();
            verifierSurConsommation();
        } catch (NumberFormatException e) {
            //jouerSonAlerte();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Erreur");
            alert.setHeaderText("Valeur non valide");
            alert.setContentText("Veuillez entrer une valeur numérique");
            alert.showAndWait();
            seuilField.setStyle("-fx-border-color: red");
        }
    }

    //la partie statistique

    private void chargerStatJournalieres(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Aujourd'hui");
        String sql = """
                SELECT strftime('%H', timestamp) AS heure, SUM(consommation) AS total
                 FROM consommations WHERE DATE(timestamp) = DATE('now')
                 GROUP BY heure
                 ORDER BY heure;
                """;
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
        )
        {
            while (rs.next()){
                String heure = rs.getString("heure") + "h";
                double total = rs.getDouble("total");
                series.getData().add(new XYChart.Data<>(heure, total));
            }
            dailyChart.getData().clear();
            dailyChart.getData().add(series);
            System.out.println(8);
            Database.getConnection().close();
        } catch (SQLException e) {
            System.out.println(1);
            throw new RuntimeException(e);
        }
    }

    private void chargeStateMensuelles(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ce mois-ci");
        String sql = """
                SELECT strftime('%d', timestamp) AS jour, SUM(consommation) AS total
                 FROM consommations WHERE strftime('%Y-%m',timestamp) = strftime('%Y-%m','now')
                 GROUP BY jour
                 ORDER BY jour;
                """;
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
        )
        {
            while (rs.next()){
                String jour = rs.getString("jour");
                double total = rs.getDouble("total");
                series.getData().add(new XYChart.Data<>(jour, total));
            }
            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
            System.out.println(7);
            Database.getConnection().close();
        } catch (SQLException e) {
            System.out.println(2);
            throw new RuntimeException(e);
        }
    }

    private void chargeStateAnnuelles(){
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cette Année");
        String sql = """
                SELECT strftime('%m', timestamp) AS mois, SUM(consommation) AS total
                 FROM consommations WHERE strftime('%Y',timestamp) = strftime('%Y','now')
                 GROUP BY mois
                 ORDER BY mois;
                """;
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql);
        )
        {
            while (rs.next()){
                String mois = rs.getString("mois");
                System.out.println(mois);
                int moisInt = Integer.parseInt(mois);
                System.out.println(moisInt);
                String moisNom = LocalDate.of(2025, moisInt, 1).getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
                System.out.println(moisNom);
                double total = rs.getDouble("total");
                series.getData().add(new XYChart.Data<>(moisNom, total));
            }
            annualyChart.getData().clear();
            annualyChart.getData().add(series);
            Database.getConnection().close();
        } catch (SQLException e) {
            System.out.println(3);
            throw new RuntimeException(e);
        }
    }

    public void jouerSonAlerte(){
        try {
            File son = new File("chemin/du/son");
            //File son = new File(getClass().getResource("chemin/du/son.wav")).toURI();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(son);
            Clip clip = AudioSystem.getClip();
            clip.start();


        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onAfficheHistorique(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/gestionenergetique/view/historique-view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Historique de consommation");
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void onAfficherHistorique(ActionEvent actionEvent) {
       String vue = comboTypeVue.getValue();
       LocalDate date = datePicker.getValue();
       if(vue == null || date == null){
           //jouerSonAlerte();
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setTitle("Erreur");
           alert.setHeaderText("Choix Invalide");
           alert.setContentText("Veuillez choisir et entrer une date");
           alert.showAndWait();
           return;
       }
       ObservableList<HistoriqueConsommation> donnees = FXCollections.observableArrayList();
       XYChart.Series<String, Number> series = new XYChart.Series<>();
       String sql = switch (vue){
           case "Journalière" -> """
                   SELECT a.nom, c.duree, c.consommation, c.timestamp
                   FROM consommations c JOIN appareils a
                   ON c.id_appareil = a.id
                   WHERE DATE(c.timestamp) = ?
                   """;
           case "Mensuelle" -> """
                   SELECT a.nom, c.duree, c.consommation, c.timestamp
                   FROM consommations c JOIN appareils a
                   ON c.id_appareil = a.id
                   WHERE strftime('%Y-%m', c.timestamp) = ?
                   """;
           case "Annuelle" -> """
                   SELECT a.nom, c.duree, c.consommation, c.timestamp
                   FROM consommations c JOIN appareils a
                   ON c.id_appareil = a.id
                   WHERE strftime('%Y', c.timestamp) = ?
                   """;
           default -> throw new IllegalStateException("Type de vue invalide");
       };
       String param = switch (vue){
           case "Journalière" -> date.toString();
           case "Mensuelle" -> date.getYear() + "_" + String.format("%02d", date.getMonthValue());
           case "Annuelle" -> String.valueOf(date.getYear());
           default -> "";
       };

       try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)){
           ps.setString(1, param);
           ResultSet rs = ps.executeQuery();
           HashMap<String, Double> aggregation = new HashMap<>();
           while (rs.next()){
               String nom = rs.getString("nom");
               int duree = rs.getInt("duree");
               double conso = rs.getDouble("consommation");
               String time = rs.getString("timestamp");

               donnees.add(new HistoriqueConsommation(nom, duree, conso, time));
               //Aggregation pour le graphique
               String key = switch (vue){
                   case "Journalière" -> time.substring(11, 13) + "h";
                   case  "Mensuelle" -> time.substring(8, 10);
                   case "Annuelle" -> time.substring(5, 7);
                   default -> "?";
               };

               aggregation.put(key, aggregation.getOrDefault(key, 0.0) + conso);
           }

           //Chart
           chartHistorique.getData().clear();
           for (var entry : aggregation.entrySet()){
               series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
           }

           chartHistorique.getData().add(series);
           tableHistorique.setItems(donnees);
           tableHistorique.refresh();

       } catch (SQLException e) {
           throw new RuntimeException(e);
       }
    }

    public long calculerDureeDepuisAllumage(int idAppareil){
        LocalDateTime debut = heureAllumageMap.get(idAppareil);
        System.out.println("début "+debut);
        if (debut == null) return 0;
        Duration duree = Duration.between(debut, LocalDateTime.now());
        System.out.println("Duréé ecoulée " +duree.getSeconds());
        return duree.getSeconds();
    }

    public double calculConsommation(double puissance, long duree){
        double dureeheures = duree/3600.0;
        return (puissance * dureeheures)/1000.0;
    }

    public static void getLoginView(){
        try {
            Parent root = FXMLLoader.load(MainController.class.getResource("/org/example/gestionenergetique/view/login-view.fxml"));
            Stage stage = (Stage) new Stage();
            stage.setScene(new Scene(root));
            //stage.setTitle("Connexion requise");
            stage.show();
            //On ferme d'abord la fenetre actuelle
            //Stage currentStage = (Stage) stage.getScene().getWindow();
            //currentStage.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        AppSession.logout();
        try {
            Parent root = FXMLLoader.load(MainController.class.getResource("/org/example/gestionenergetique/view/login-view.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onPlannifierHeure(ActionEvent actionEvent) {
        Appareil appareil = tableAppareils.getSelectionModel().getSelectedItem();
        if (appareil == null){
            showAlerte("Aucun appareil selectionné");
            return;
        }
        String onTime = heureOnField.getText().trim();
        String offTime = heureOffField.getText().trim();
        System.out.println(onTime+ " "+ offTime);
        if (!onTime.isEmpty() && offTime.isEmpty()){
            offTime = null;
            appareil.setHeurOn(onTime);
            appareil.setheureOff(offTime);
            appareilDAO.mettreAJourHeuresPlannification(appareil);
            showAlerte("Plannification enregistré d'allumage pour "+ appareil.getNomApareil() + " à "+ appareil.getHeureOn());
        }
        if (onTime.isEmpty() && !offTime.isEmpty()){
            onTime = null;
            appareil.setHeurOn(onTime);
            appareil.setheureOff(offTime);
            appareilDAO.mettreAJourHeuresPlannification(appareil);
            showAlerte("Plannification enregistré d'extinction pour "+ appareil.getNomApareil() + " à "+ appareil.getHeureOff());
        }
        if (!onTime.isEmpty() && !offTime.isEmpty()){
            appareil.setHeurOn(onTime);
            appareil.setheureOff(offTime);
            appareilDAO.mettreAJourHeuresPlannification(appareil);
            showAlerte("Plannification enregistré d'allumage et d'extinction pour "+ appareil.getNomApareil() + " à "+ appareil.getHeureOn()+ " et "+ appareil.getHeureOff());
        }
        System.out.println(" OnOff" +appareil.getHeureOn() + " "+ appareil.getHeureOff());

    }

    private void showAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void onAfficherAnalyse(ActionEvent actionEvent) throws SQLException {
        LocalDateTime debut = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now();

        Map<String, Double> map = consommationDAO.getConsommationParAppareilSurPeriode(debut, fin);
        listeComparative.getItems().clear();
        for(Map.Entry<String, Double> entry : map.entrySet()){
            listeComparative.getItems().add(entry.getKey() + " n " + String.format("%.2f kwh", entry.getValue()));
        }
    }

    @FXML
    public void onEstimerFacture(ActionEvent actionEvent) {
        try {
            double prixKwh = Double.parseDouble(prixKwhField.getText());
            LocalDateTime debut = LocalDateTime.now().minusDays(30);
            LocalDateTime fin = LocalDateTime.now();

            double totalConso = consommationDAO.getConsommationTotalSurPeriode(debut, fin);
            double facture = totalConso * prixKwh;

            resultatFactureLabel.setText("Estimation mensuel : " + String.format("%.2f €", facture));
            //parametreDAO.set("prix_kwh", String.valueOf(prixKwh));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    //Section categorisation

}
