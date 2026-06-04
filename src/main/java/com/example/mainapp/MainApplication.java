package com.example.mainapp;

import com.example.mainapp.controller.MainController;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;
import com.example.mainapp.utils.ConfigManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        Company maCompagnie = PersistenceManager.loadData();
        if (maCompagnie == null) {
            maCompagnie = new Company("Polytech Tours");
            System.out.println("Aucune donnée trouvée. Création d'une entreprise par défaut.");
        }

        ConfigManager config = new ConfigManager();

        TCPServer.getInstance().demarrer(config.getServerPort(), maCompagnie);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        stage.setTitle("Système de Gestion de Pointage");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping application...");
        MainController mainController = MainController.instance;
        if (mainController != null) {
            mainController.cleanup();
        }

        TCPServer.getInstance().arreter();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}