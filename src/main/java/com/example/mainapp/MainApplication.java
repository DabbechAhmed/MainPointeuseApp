package com.example.mainapp;

import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.service.PersistenceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Chargement des données de l'entreprise depuis le fichier binaire
        Company maCompagnie = PersistenceManager.loadData();
        if (maCompagnie == null) {
            // Sécurité si aucun fichier company_data.ser n'est encore enregistré
            maCompagnie = new Company("Polytech Tours");
            System.out.println("ℹ️ Aucune donnée trouvée. Création d'une entreprise par défaut.");
        }

        // 2. Démarrage du serveur TCP sur le port 8080 en lui donnant les données chargées
        TCPServer.getInstance().demarrer(8080, maCompagnie);

        // 3. Chargement de l'interface graphique
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("Système de Gestion de Pointage");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping application...");
        // Extinction propre du serveur
        TCPServer.getInstance().arreter();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}