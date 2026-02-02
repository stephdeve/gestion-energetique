package org.example.gestionenergetique.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import javafx.util.Pair;
import org.example.gestionenergetique.DAO.AppareilDAO;
import org.example.gestionenergetique.DAO.ConsommationDAO;
import org.example.gestionenergetique.DAO.Database;
import org.example.gestionenergetique.DAO.HistoriqueDAO;
import org.example.gestionenergetique.model.AppareilConsommation;
import org.example.gestionenergetique.model.ConsommationHistorique;
import org.example.gestionenergetique.model.DataPoint;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML public ComboBox<String> periodeComboBox;
    @FXML public LineChart<String, Number> mainChart;
    @FXML public Label labelStat;
    @FXML public PieChart pieChartAppareils;
    @FXML public Label labelStatInactif;
    public AppareilDAO appareilDAO = new AppareilDAO();
    @FXML public TableView<AppareilConsommation> recentDevicesTable;
    @FXML public TableColumn<AppareilConsommation, String> colRecentDevice;
    @FXML public TableColumn<AppareilConsommation, Number> colRecentUsage;
    @FXML public TableColumn<AppareilConsommation, String> colRecentDuration;
    @FXML public TableColumn<AppareilConsommation, String> colTimestamp;
    @FXML public Label labelConso;
    @FXML public Label labelPlus;
    @FXML public Label labelDiff;

    public void initialize() throws SQLException {
       afficherStatAppareils();
       demarrerMiseAjourAuto();
       initialiserTable();
       chargerDonneesConsommation();
    }

    public void afficherStatAppareils(){
        Pair<Integer, Integer> stats = appareilDAO.getNbAppareilActifsEtTotal();
        int actifs = stats.getKey();
        int total = stats.getValue();
        int inactifs = total-actifs;
        labelStat.setText(actifs+"/"+total);
        labelStatInactif.setText(inactifs+"/"+total);
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList(
                new PieChart.Data("Actifs", actifs),
                new PieChart.Data("Inactifs", inactifs)
        );
        pieChartAppareils.setData(chartData);
        pieChartAppareils.setTitle("Etat des appareils");

    }

    public void demarrerMiseAjourAuto(){
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), actionEvent -> {
            afficherStatAppareils();
            try {
                updateConsommation();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);//on repète indefinement
        timeline.play();
    }
    public void initialiserTable(){
        colRecentDevice.setCellValueFactory(data -> data.getValue().nomAppareilProperty());
        colRecentUsage.setCellValueFactory(data -> data.getValue().consommationAppareilProperty());
        colRecentDuration.setCellValueFactory(data -> data.getValue().dureeProperty());
        colTimestamp.setCellValueFactory(data -> data.getValue().timestampProperty());
    }
    public void chargerDonneesConsommation() throws SQLException {
        AppareilDAO appareilDAO = new AppareilDAO();
        ObservableList<AppareilConsommation> appareilLists = appareilDAO.chargerDonneesConsommation();
        recentDevicesTable.setItems(appareilLists);

    }

    @FXML
    public void onPeriodeChange(ActionEvent actionEvent) {
        String periode = periodeComboBox.getValue();
        List<DataPoint> data = getDataForPeriode(periode);
        updateChart(data);
    }

    private void updateChart(List<DataPoint> data) {
        mainChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Consommation");
        for(DataPoint point : data){
            series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
        }
        mainChart.getData().add(series);
    }

    private List<DataPoint> getDataForPeriode(String periode) {
        List<DataPoint> data = new ArrayList<>();

        String sql = switch (periode) {
            case "24 heures" -> """
                    SELECT strftime('%H',timestamp) AS heure, SUM(consommation) AS consommation FROM consommations WHERE timestamp >= datetime('now', '-1 day') GROUP BY heure
                    """;
            case "7 jours" -> """
                    SELECT strftime('%Y-%m-%d', timestamp) AS jour, SUM(consommation) AS consommation FROM consommations WHERE timestamp >= datetime('now', '-7 days') GROUP BY jour
                    """;
            case "1 mois" -> """
                    SELECT strftime('%Y-%m-%d', timestamp) AS jour, SUM(consommation) AS consommation FROM consommations WHERE timestamp >= datetime('now', '-1 month') GROUP BY jour
                    """;
            case "1 an" -> """
                    SELECT strftime('%Y-%m', timestamp) AS mois, SUM(consommation) AS consommation FROM consommations WHERE timestamp >= datetime('now', '-1 year') GROUP BY mois
                    """;
            default -> "";
        };

        try(Statement stmt = Database.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    String label = rs.getString(1);
                    double value = rs.getDouble(2);
                    data.add(new DataPoint(label, value));
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    public void updateConsommation() throws SQLException {
        ConsommationDAO consommationDAO = new ConsommationDAO();
        double totalToDay = 0;
        double totalYesterDay = 0;

        totalToDay = consommationDAO.getTotalToDay();
        totalYesterDay = consommationDAO.getTotalYesterDay();
        double difference = totalToDay - totalYesterDay;

        String diffStr = (difference >= 0 ? "+" : "") + String.format("%.2f", difference) + "KW vs hier";
        labelConso.setText(String.format("%.2f KW", totalToDay));
        labelDiff.setText(diffStr);

        if (difference > 0){
            labelDiff.setStyle("-fx-text-fill: green; -fx-font-size: 12;\n" +
                    "     -fx-font-weight: bold;");
        } else if (difference < 0) {
            labelDiff.setStyle("-fx-text-fill: red; -fx-font-size: 12;\n" +
                    "     -fx-font-weight: bold;");
        }else {
            labelDiff.setStyle("-fx-text-fill: gray; -fx-font-size: 12;\n" +
                    "     -fx-font-weight: bold;");
        }
    }
}
