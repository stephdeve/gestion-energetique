package org.example.gestionenergetique;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws  Exception{
        // Initialisation des polices Ikonli
        // Font.loadFont(MaterialDesignIconView.class.getResource("/de/jensd/fx/glyphs/materialdesignicons/materialdesignicons-webfont.ttf").toExternalForm(), 16);
        //Font.loadFont(FontAwesomeIconView.class.getResource("/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf").toExternalForm(), 16);
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("view/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Gestion Energie Maison");
        primaryStage.setScene(scene);
        /*if (!AppSession.estConnecte()){
            System.out.println("Acces refusé: Aucun utilisateur connecté");
            MainController.getLoginView();
            return;
        }*/
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
