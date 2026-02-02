package org.example.gestionenergetique.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import org.example.gestionenergetique.DAO.HistoriqueDAO;
import org.example.gestionenergetique.model.ConsommationHistorique;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class HistoriqueController {

    @FXML public TableView<ConsommationHistorique> tableHistorique;
    @FXML public TableColumn<ConsommationHistorique, String> colDate;
    @FXML public TableColumn<ConsommationHistorique, Double> colTotal;
    @FXML public TextField searchField;
    @FXML public TableColumn<ConsommationHistorique, String> colAppareil;
    @FXML public TableColumn<ConsommationHistorique, Double> colDuree;
    @FXML public TableColumn<ConsommationHistorique, Integer> colId;
    @FXML public DatePicker dateDebutPicker;
    @FXML public DatePicker dateFinPicker;

    private final HistoriqueDAO historiqueDAO = new HistoriqueDAO();
    private final ObservableList<ConsommationHistorique> historiqueCache = FXCollections.observableArrayList();
    private FilteredList<ConsommationHistorique> filteredHistorique;

    @FXML
    public void initialize() {
        setupColumns();
        loadHistorique();
    }



    private void setupColumns(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAppareil.setCellValueFactory(new PropertyValueFactory<>("appareil"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("consommation"));
    }

    private void loadHistorique(){
        historiqueCache.setAll(historiqueDAO.LoadHistorique());
        if (filteredHistorique == null){
            filteredHistorique = new FilteredList<>(historiqueCache, h -> true);
            tableHistorique.setItems(filteredHistorique);
        }
    }
    @FXML
    public void onFiltre(ActionEvent actionEvent) {
        String nomAppareil = searchField.getText() != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();

        if (filteredHistorique == null){
            filteredHistorique = new FilteredList<>(historiqueCache, h -> true);
            tableHistorique.setItems(filteredHistorique);
        }

        filteredHistorique.setPredicate(historique -> {
            if (historique == null){
                return false;
            }

            boolean matchNom = nomAppareil.isBlank() ||
                    (historique.getAppareil() != null && historique.getAppareil().toLowerCase(Locale.ROOT).contains(nomAppareil));

            LocalDate dateConsommation = parseDate(historique.getDate());
            boolean matchDebut = dateDebut == null || dateConsommation == null || !dateConsommation.isBefore(dateDebut);
            boolean matchFin = dateFin == null || dateConsommation == null || !dateConsommation.isAfter(dateFin);

            return matchNom && matchDebut && matchFin;
        });
    }

    private LocalDate parseDate(String value){
        if (value == null || value.isBlank()){
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e){
            // tentative avec format alternatif (ex: dd/MM/yyyy)
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored){
                return null;
            }
        }
    }
}
