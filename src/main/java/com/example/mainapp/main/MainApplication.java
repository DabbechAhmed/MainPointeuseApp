package com.example.mainapp.main;

import com.example.mainapp.controller.MainController;
import com.example.mainapp.model.company.Company;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;
import com.example.mainapp.utils.ConfigManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Point d'entrée principal de l'application centrale d'administration (Serveur RH).
 * <p>
 * Cette classe orchestre le cycle de vie de l'application JavaFX. Elle prend en charge
 * le chargement initial des données de l'entreprise, l'initialisation de la configuration,
 * le démarrage en arrière-plan du serveur TCP multithread, ainsi que le déploiement
 * et l'affichage de l'interface graphique principale.
 * </p>
 * * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class MainApplication extends Application {

    /**
     * Initialise et démarre les composants fondamentaux et graphiques du système.
     * <p>
     * Cette méthode récupère l'état persistant de l'entreprise, instancie les paramètres
     * réseau, initie l'écoute du serveur TCP sur le port configuré, puis charge la vue
     * principale FXML de l'application.
     * </p>
     *
     * @param stage Le théâtre (Stage) principal fourni par l'environnement JavaFX.
     * @throws IOException Si le fichier de vue FXML principal ne peut pas être localisé ou lu.
     */
    @Override
    public void start(Stage stage) throws IOException {

        // Chargement des données de l'entreprise précédemment sérialisées
        Company maCompagnie = PersistenceManager.loadData();
        if (maCompagnie == null) {
            maCompagnie = new Company("Polytech Tours");
            System.out.println("Aucune donnée trouvée. Création d'une entreprise par défaut.");
        }

        // Instanciation du gestionnaire de configuration locale
        ConfigManager config = new ConfigManager();

        // Démarrage du serveur TCP d'écoute pour la réception des flux de pointage
        TCPServer.getInstance().demarrer(config.getServerPort(), maCompagnie);

        // Configuration et affichage de la scène graphique JavaFX principale
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1300, 700);
        stage.setTitle("Système de Gestion de Pointage");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Gère la fermeture propre de l'application et la libération des ressources.
     * <p>
     * Interceptée lors de la fermeture de la fenêtre, cette méthode déclenche le nettoyage
     * des threads des contrôleurs de l'IHM et ordonne l'arrêt complet des threads du serveur
     * et des canaux de communication TCP encore actifs.
     * </p>
     *
     * @throws Exception Si une erreur survient lors du processus d'extinction des services.
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Stopping application...");

        // Libération des ressources associées au contrôleur principal
        MainController mainController = MainController.instance;
        if (mainController != null) {
            mainController.cleanup();
        }

        // Interruption des écoutes réseau et fermeture des sockets de communication
        TCPServer.getInstance().arreter();
        super.stop();
    }

    /**
     * Point d'entrée de la machine virtuelle Java lançant le cycle d'exécution.
     *
     * @param args Les arguments de la ligne de commande transmis au programme.
     */
    public static void main(String[] args) {
        launch();
    }
}