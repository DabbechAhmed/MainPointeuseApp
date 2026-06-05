package com.example.mainapp.controller.settings;

import com.example.mainapp.utils.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.net.InetAddress;

public class SettingsController {

    @FXML private TextField serverIpField;
    @FXML private TextField serverPortField;
    @FXML private TextField toleranceField;

    private ConfigManager config;

    @FXML
    public void initialize() {
        this.config = new ConfigManager();

        serverPortField.setText(String.valueOf(config.getServerPort()));
        toleranceField.setText(String.valueOf(config.getToleranceMinutes()));

        try {
            String ipMachine = InetAddress.getLocalHost().getHostAddress();
            serverIpField.setText(ipMachine);
        } catch (Exception e) {
            serverIpField.setText("127.0.0.1 (Localhost)");
        }
    }

    @FXML
    protected void handleSaveSettings() {
        try {
            int port = Integer.parseInt(serverPortField.getText());
            int tolerance = Integer.parseInt(toleranceField.getText());
            config.setServerPort(port);
            config.setToleranceMinutes(tolerance);
            config.saveConfig();

            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Paramètres sauvegardés avec succès !\n(Le changement de port nécessite un redémarrage de l'application)");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Le port et la tolérance doivent être des nombres entiers !");
            alert.showAndWait();
        }
    }
}