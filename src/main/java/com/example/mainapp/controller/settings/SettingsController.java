package com.example.mainapp.controller.settings;

import com.example.mainapp.utils.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.net.InetAddress;

/**
 * Contrôleur de la vue des paramètres globaux de l'application centrale (Serveur).
 * <p>
 * Cette classe gère l'interface permettant à l'administrateur RH de configurer
 * les paramètres techniques du serveur (port d'écoute TCP) ainsi que les règles
 * métier (tolérance en minutes pour les pointages). Elle détecte et affiche également
 * l'adresse IP locale de la machine hôte pour faciliter la configuration des pointeuses clientes.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class SettingsController {

    /** Champ d'affichage de l'adresse IP réseau de la machine hébergeant le serveur. */
    @FXML private TextField serverIpField;

    /** Champ de saisie pour définir le port d'écoute TCP du serveur. */
    @FXML private TextField serverPortField;

    /** Champ de saisie pour définir la tolérance de pointage (en minutes). */
    @FXML private TextField toleranceField;

    /** Gestionnaire utilitaire pour la lecture et l'écriture du fichier de configuration. */
    private ConfigManager config;

    /**
     * Initialise le contrôleur lors du chargement de la vue JavaFX.
     * <p>
     * Cette méthode pré-remplit les champs avec les paramètres actuellement
     * sauvegardés via le {@link ConfigManager}. Elle tente également de résoudre
     * dynamiquement l'adresse IP réseau de la machine pour l'afficher à l'administrateur.
     * En cas d'échec de résolution, l'adresse de boucle locale (127.0.0.1) est affichée par défaut.
     * </p>
     */
    @FXML
    public void initialize() {
        this.config = new ConfigManager();

        serverPortField.setText(String.valueOf(config.getServerPort()));
        toleranceField.setText(String.valueOf(config.getToleranceMinutes()));

        try {
            // Résolution dynamique de l'IP pour aiguiller l'utilisateur
            String ipMachine = InetAddress.getLocalHost().getHostAddress();
            serverIpField.setText(ipMachine);
        } catch (Exception e) {
            serverIpField.setText("127.0.0.1 (Localhost)");
        }
    }

    /**
     * Traite l'action de sauvegarde des paramètres déclenchée par l'administrateur.
     * <p>
     * La méthode intercepte la saisie, vérifie que les valeurs (port et tolérance)
     * sont des entiers syntaxiquement valides (gestion de l'exception {@link NumberFormatException}).
     * Si la validation réussit, les nouvelles valeurs sont déléguées au gestionnaire
     * de configuration pour être persistées sur le disque dur.
     * </p>
     */
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