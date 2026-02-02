package org.example.gestionenergetique.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.example.gestionenergetique.DAO.ConsommationDAO;
import org.example.gestionenergetique.DAO.ParametreDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class FactureController {
   @FXML public Label labelFactureJour;
    @FXML public Label labelFactureMois;
    @FXML public Label labelFactureAnnee;
    @FXML public Label labelPrevisionFinMois;
    @FXML public Label labelMoisPrecedent;
    @FXML public BarChart<String, Number> barChartFacture;

    private final ConsommationDAO consommationDAO = new ConsommationDAO();
    private final ParametreDAO parametreDAO = new ParametreDAO();

    public void initialize(){
        //coût du jour
        LocalDate aujourdHui = LocalDate.now();
        double coutJour = calculerCout(aujourdHui, aujourdHui);
        //Coût du mois en cours
        LocalDate debutMois = aujourdHui.withDayOfMonth(1);
        double coutMois = calculerCout(debutMois, aujourdHui);
        //Coût Année
        LocalDate debutAnnee = aujourdHui.withDayOfYear(1);
        double coutAnnee = calculerCout(debutAnnee, aujourdHui);

        //Mois précédent
        LocalDate moisPrec  = aujourdHui.minusMonths(1).withDayOfMonth(1);
        LocalDate  finMoisPrec = moisPrec.withDayOfMonth(moisPrec.lengthOfMonth());
        double coutMoisPrec  = calculerCout(moisPrec, finMoisPrec);

        //Prévision fin de mois
        double prevision = getEstimationFinMois();
        //Mise à jour de l'intterface utilisateur
        labelFactureJour.setText(formatMontant(coutJour));
        labelFactureMois.setText(formatMontant(coutMois));
        labelFactureAnnee.setText(formatMontant(coutAnnee));
        labelMoisPrecedent.setText(formatMontant(coutMoisPrec));
        labelPrevisionFinMois.setText(formatMontant(prevision));

        afficherGraphique(coutMoisPrec, coutJour, prevision);
    }

    private double getPrixKwh(){
        try{
            return Double.parseDouble(parametreDAO.getValeur("prix_kwh"));
        } catch (Exception e) {
            return 0.15;
        }
    }

    private double calculerCout(LocalDate debut, LocalDate fin){
        double totalConsommation = consommationDAO.getConsommationTotal(debut, fin);
        return totalConsommation *  getPrixKwh();
    }

    private double getEstimationFinMois(){
        LocalDate now = LocalDate.now();
        LocalDate debut = now.withDayOfMonth(1);
        long joursPasses = ChronoUnit.DAYS.between(debut, now) + 1; // inclure la journée en cours
        if (joursPasses <= 0){
            joursPasses = 1;
        }
        double coutActuel = calculerCout(debut, now);
        long joursTotal = now.lengthOfMonth();
        if (coutActuel <= 0){
            return 0;
        }
        double estimation = (coutActuel / joursPasses) * joursTotal;
        if (Double.isNaN(estimation) || Double.isInfinite(estimation)){
            return coutActuel;
        }
        return estimation;
    }

    private void afficherGraphique(double precedent, double actuel, double prevision){
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Facture");

        serie.getData().add(new XYChart.Data<>("Mois Précédent", precedent));
        serie.getData().add(new XYChart.Data<>("Mois Actuel", actuel));
        serie.getData().add(new XYChart.Data<>("Prévision", prevision));

        barChartFacture.getData().clear();
        barChartFacture.getData().add(serie);
    }

    private String formatMontant(double montant){
        return String.format(Locale.FRANCE, "%.2f €", montant);
    }
}
