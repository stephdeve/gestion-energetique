package org.example.gestionenergetique.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.gestionenergetique.DAO.ConsommationDAO;
import org.example.gestionenergetique.model.AppareilConsommation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalyseController {
    @FXML public TableView<AppareilConsommation> tableTopAppareils;
    @FXML public TableColumn<AppareilConsommation, String> colNomTop;
    @FXML public TableColumn<AppareilConsommation, Double> colConsoTop;
    @FXML public TableColumn<AppareilConsommation, String> colPourcentage;
    @FXML public BarChart<String, Number> chartHeures;
    @FXML public ListView<String> listSuggestions;

    private final ConsommationDAO consommationDAO = new ConsommationDAO();
    private final double consoTotalMois = consommationDAO.getConsommationTotal(LocalDate.now().withDayOfMonth(1), LocalDate.now());

    public void initialize(){
        Platform.runLater(() ->{
            chargerTopAppareils();
            chargerGraphiqueHeures();
            chargerSuggestions();
        });

    }

    private void chargerTopAppareils(){
        List<AppareilConsommation> topAppareils = consommationDAO.getTopConsommateurs(5);
        ObservableList<AppareilConsommation> data = FXCollections.observableArrayList(topAppareils);
       // colNomTop.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNomTop.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        colConsoTop.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getConsommation()));
        //colConsoTop.setCellValueFactory(new PropertyValueFactory<>("consommation"));

        colPourcentage.setCellValueFactory(cellData -> {
            double conso = cellData.getValue().getConsommation();
            double pourcent = (consoTotalMois == 0)? 0 : (conso / consoTotalMois) * 100;
            return new SimpleStringProperty(String.format("%.1f %%", pourcent));
        });
        tableTopAppareils.setItems(data);
    }

    private void chargerGraphiqueHeures(){
        Map<String, Double> consoParHeure = consommationDAO.getConsoParHeure();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        consoParHeure.forEach((heure, conso)-> serie.getData().add(new XYChart.Data<>(heure , conso)));
        chartHeures.getData().clear();
        chartHeures.getData().add(serie);
    }

    private void chargerSuggestions(){
        List<String> suggestions = new ArrayList<>();

        if (consoTotalMois > 100){
            suggestions.add("Vous dépassez 100 Kwh ce mois. Réduisez l'usage des gros appareils.");
        }

        List<AppareilConsommation> top = consommationDAO.getTopConsommateurs(1);
        if (!top.isEmpty() && top.getFirst().getConsommation() > 50){
            suggestions.add("L'appareil" + top.getFirst().getNom() + " consomme énormément. Pensez à l'utiliser moins fréquemment.");
        }

        suggestions.add("Pensez à planifier automatiquement les appareils non essentiels.");

        listSuggestions.setItems(FXCollections.observableArrayList(suggestions));
    }
}
