package com.example.mainapp.controller;

import com.example.mainapp.controller.attendance.AttendanceController;
import com.example.mainapp.controller.departement.DepartmentController;
import com.example.mainapp.controller.employee.EmployeeController;
import com.example.mainapp.controller.settings.SettingsController;
import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.network.TCPServer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Contrôleur maître de l'interface graphique principale de l'application centrale.
 * <p>
 * Cette classe orchestre l'agencement global de la fenêtre (BorderPane) en gérant le menu latéral de
 * navigation et le basculement dynamique des vues imbriquées au sein d'un StackPane. Elle implémente
 * le mécanisme d'injection de sous-contrôleurs (Nested Controllers) de JavaFX pour déléguer les
 * actions spécifiques à chaque domaine métier.
 * </p>
 * <p>
 * <b>Architecture Multi-thread & Résilience :</b> Pour permettre aux threads réseau d'arrière-plan
 * (les {@code ClientHandler} du serveur TCP) de notifier l'interface graphique lors de la réception
 * d'un nouveau pointage sans créer un couplage lourd, cette classe expose une référence statique globale
 * (pseudo-singleton). Les mises à jour graphiques asynchrones demandées par le réseau sont ainsi injectées
 * de manière sécurisée dans le thread JavaFX principal.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class MainController {

    /** * Référence statique globale (pseudo-singleton) vers la seule instance active de l'interface visuelle.
     * <p>
     * <b>Attention de conception :</b> Cette variable est initialisée automatiquement lors du cycle de vie
     * JavaFX dans la méthode {@link #initialize()} et est expressément consommée par la couche réseau
     * pour forcer le rafraîchissement des tableaux d'affichage de manière asynchrone via {@code Platform.runLater()}.
     * </p>
     */
    public static MainController instance;

    /** L'instance centrale de l'entreprise contenant les listes d'employés et de pointages. */
    private Company company;

    @FXML /** Conteneur graphique de la vue du Tableau de bord. */
    private VBox viewDashboard;

    @FXML /** Conteneur graphique de la vue de gestion des employés. */
    private VBox viewEmployees;

    @FXML /** Conteneur graphique de la vue de gestion des départements. */
    private VBox viewDepartments;

    @FXML /** Conteneur graphique de la vue de l'historique des pointages. */
    private VBox viewPointages;

    @FXML /** Conteneur graphique de la vue des configurations système. */
    private VBox viewSettings;

    @FXML /** Sous-contrôleur injecté automatiquement pour la gestion de l'interface des employés. */
    private EmployeeController viewEmployeesController;

    @FXML /** Sous-contrôleur injecté automatiquement pour la gestion de l'interface des départements. */
    private DepartmentController viewDepartmentsController;

    @FXML /** Sous-contrôleur injecté automatiquement pour la gestion de l'interface des pointages. */
    private AttendanceController viewPointagesController;

    @FXML /** Sous-contrôleur injecté automatiquement pour la gestion de l'interface des paramètres. */
    private SettingsController viewSettingsController;

    @FXML /** Label affichant l'état ou les messages de journalisation au bas de la barre latérale. */
    private Label statusLabel;

    @FXML /** Carte statistique affichant l'effectif total des employés. */
    private Label employeeCountLabel;

    @FXML /** Carte statistique affichant le volume de pointages enregistrés aujourd'hui. */
    private Label pointagesTodayLabel;

    @FXML /** Carte statistique affichant le nombre d'incidents ou de retards détectés aujourd'hui. */
    private Label incidentsLabel;

    @FXML /** Bouton de navigation du menu latéral pour afficher le tableau de bord. */
    private Button btnDashboard;

    @FXML /** Bouton de navigation du menu latéral pour afficher la vue des employés. */
    private Button btnEmployees;

    @FXML /** Bouton de navigation du menu latéral pour afficher la vue des départements. */
    private Button btnDepartments;

    @FXML /** Bouton de navigation du menu latéral pour afficher la vue des pointages. */
    private Button btnPointages;

    @FXML /** Bouton de navigation du menu latéral pour afficher la vue des paramètres. */
    private Button btnSettings;

    /**
     * Méthode d'initialisation automatique du cycle de vie de JavaFX.
     * <p>
     * Assigne l'instance courante à la référence statique, configure le message d'accueil initial
     * dans la barre de statut, charge les données au sein des différents tableaux graphiques via
     * {@link #loadDataIntoTables()} et force l'affichage initial sur le tableau de bord.
     * </p>
     */
    @FXML
    public void initialize() {
        instance = this;
        statusLabel.setText("Application démarrée");

        loadDataIntoTables();
        showDashboard();
    }

    /**
     * Déclenche un rafraîchissement global et synchrone de tous les composants de l'interface utilisateur.
     */
    public void rafraichirUI() {
        loadDataIntoTables();
    }

    /**
     * Calcule et met à jour les indicateurs numériques et statistiques affichés sur le Tableau de bord.
     * <p>
     * Cette méthode récupère l'instance métier de l'entreprise depuis le serveur TCP, mesure la taille
     * de l'effectif, puis parcourt l'historique complet des pointages pour comptabiliser le nombre
     * d'entrées/sorties de la journée courante ainsi que les anomalies associées (incidents et retards).
     * </p>
     */
    public void rafraichirDashboard() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        if (employeeCountLabel != null) {
            employeeCountLabel.setText(String.valueOf(company.getEmployees().size()));
        }

        LocalDate aujourdhui = java.time.LocalDate.now();
        int countPointages = 0;
        int countIncidents = 0;

        if (company.getAttendanceRecords() != null) {
            for (AttendanceRecord record : company.getAttendanceRecords()) {
                if (record == null || record.getTime() == null) continue;

                if (record.getTime().toLocalDate().equals(aujourdhui)) {
                    countPointages++;
                    String status = record.getStatus() != null ? record.getStatus().toLowerCase() : "";
                    if (status.contains("incident") || status.contains("retard")) {
                        countIncidents++;
                    }
                }
            }
        }

        if (pointagesTodayLabel != null) {
            pointagesTodayLabel.setText(String.valueOf(countPointages));
        }
        if (incidentsLabel != null) {
            incidentsLabel.setText(String.valueOf(countIncidents));
        }
    }

    /**
     * Synchronise le modèle de données de l'entreprise avec les sous-contrôleurs de tableaux.
     * <p>
     * Interroge le serveur TCP pour obtenir les données à jour, puis appelle sélectivement les méthodes
     * de rafraîchissement des sous-contrôleurs injectés avant de mettre à jour le tableau de bord.
     * </p>
     */
    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        if (viewEmployeesController != null) viewEmployeesController.refreshTable();
        if (viewDepartmentsController != null) viewDepartmentsController.refreshTable();
        if (viewPointagesController != null) viewPointagesController.refreshTable();

        rafraichirDashboard();
    }

    /**
     * Gère l'état visuel actif/inactif des boutons du menu latéral de navigation.
     * <p>
     * Nettoie les classes CSS de style de tous les boutons du menu pour supprimer l'état {@code active},
     * puis applique la classe CSS {@code active} exclusivement sur le bouton qui vient d'être cliqué.
     * </p>
     *
     * @param clickedButton Le bouton graphique ayant reçu l'action de clic.
     */
    private void setActiveButton(Button clickedButton) {
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("active");
        if (btnEmployees != null) btnEmployees.getStyleClass().remove("active");
        if (btnDepartments != null) btnDepartments.getStyleClass().remove("active");
        if (btnPointages != null) btnPointages.getStyleClass().remove("active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("active");

        if (clickedButton != null && !clickedButton.getStyleClass().contains("active")) {
            clickedButton.getStyleClass().add("active");
        }
    }

    /**
     * Effectue la permutation des vues graphiques au centre de l'application.
     * <p>
     * Masque l'intégralité des conteneurs {@link VBox} gérés dans le StackPane principal, rend
     * visible le conteneur demandé, puis délègue la mise à jour esthétique du menu à {@link #setActiveButton(Button)}.
     * </p>
     *
     * @param viewToActivate Le conteneur {@link VBox} de la vue à afficher au premier plan.
     * @param activeBtn      Le bouton du menu correspondant à la vue activée.
     */
    private void switchView(VBox viewToActivate, Button activeBtn) {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewEmployees != null) viewEmployees.setVisible(false);
        if (viewDepartments != null) viewDepartments.setVisible(false);
        if (viewPointages != null) viewPointages.setVisible(false);
        if (viewSettings != null) viewSettings.setVisible(false);

        if (viewToActivate != null) viewToActivate.setVisible(true);

        setActiveButton(activeBtn);
    }

    /**
     * Action FXML déclenchant l'affichage du Tableau de bord.
     * <p>
     * Recalcule au préalable les indicateurs statistiques avant de permuter la vue.
     * </p>
     */
    @FXML
    protected void showDashboard() {
        rafraichirDashboard();
        switchView(viewDashboard, btnDashboard);
    }

    /**
     * Action FXML déclenchant l'affichage de l'interface de gestion des employés.
     */
    @FXML
    protected void showEmployees() {
        switchView(viewEmployees, btnEmployees);
    }

    /**
     * Action FXML déclenchant l'affichage de l'interface de gestion des départements.
     */
    @FXML
    protected void showDepartments() {
        switchView(viewDepartments, btnDepartments);
    }

    /**
     * Action FXML déclenchant l'affichage de l'interface de l'historique des pointages.
     */
    @FXML
    protected void showPointages() {
        switchView(viewPointages, btnPointages);
    }

    /**
     * Action FXML déclenchant l'affichage de l'interface des paramètres du système.
     */
    @FXML
    protected void showSettings() {
        switchView(viewSettings, btnSettings);
    }

    /**
     * Action FXML déclenchée par le bouton de rafraîchissement manuel de l'interface utilisateur.
     * <p>
     * Notifie l'utilisateur via la barre de statut avant d'exécuter l'actualisation complète des données.
     * </p>
     */
    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        rafraichirUI();
    }

    /**
     * Libère les ressources et nettoie la référence statique lors de la fermeture du contrôleur.
     * <p>
     * Cette méthode prévient les fuites de mémoire en cassant le lien du pseudo-singleton global.
     * </p>
     */
    public void cleanup() {
        instance = null;
    }
}