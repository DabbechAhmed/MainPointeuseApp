package com.example.mainapp.controller;

import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.example.mainapp.utils.ConfigManager;

public class MainController {

    public static MainController instance;
    private Company company;
    private ConfigManager config;

    // ========================================================
    // 🔀 GESTION DES PAGES (VUES FXML)
    // ========================================================
    @FXML private VBox viewDashboard;
    @FXML private VBox viewEmployees;
    @FXML private VBox viewDepartments;
    @FXML private VBox viewPointages;
    @FXML private VBox viewSettings; // ✅ NOUVEAU

    // ✅ Sous-contrôleurs injectés
    @FXML private EmployeeController viewEmployeesController;
    @FXML private DepartmentController viewDepartmentsController;
    @FXML private PointageController viewPointagesController; // ✅ NOUVEAU

    // ========================================================
    // 📊 ÉLÉMENTS DE L'INTERFACE (Dashboard & Paramètres)
    // ========================================================
    @FXML private Label statusLabel;
    @FXML private Label employeeCountLabel;

    @FXML private TextField serverPortField;
    @FXML private TextField toleranceField;

    @FXML
    public void initialize() {
        instance = this;
        statusLabel.setText("Application démarrée");

        // Initialisation de la configuration
        this.config = new ConfigManager();
        serverPortField.setText(String.valueOf(config.getServerPort()));
        toleranceField.setText(String.valueOf(config.getToleranceMinutes()));

        loadDataIntoTables();
        showDashboard();
    }

    public void rafraichirUI() {
        loadDataIntoTables();
    }

    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        // ✅ Le MainController délègue tout l'affichage aux sous-contrôleurs !
        if (viewEmployeesController != null) viewEmployeesController.rafraichirTableau();
        if (viewDepartmentsController != null) viewDepartmentsController.rafraichirTableau();
        if (viewPointagesController != null) viewPointagesController.rafraichirTableau();

        // Mise à jour des stats du Dashboard
        employeeCountLabel.setText(String.valueOf(company.getEmployees().size()));
    }

    // ========================================================
    // 🧭 NAVIGATION (MÉTHODE OPTIMISÉE)
    // ========================================================
    private void switchView(VBox viewToActivate) {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewEmployees != null) viewEmployees.setVisible(false);
        if (viewDepartments != null) viewDepartments.setVisible(false);
        if (viewPointages != null) viewPointages.setVisible(false);
        if (viewSettings != null) viewSettings.setVisible(false);

        if (viewToActivate != null) viewToActivate.setVisible(true);
    }

    @FXML protected void showDashboard() { switchView(viewDashboard); }
    @FXML protected void showEmployees() { switchView(viewEmployees); }
    @FXML protected void showDepartments() { switchView(viewDepartments); }
    @FXML protected void showPointages() { switchView(viewPointages); }
    @FXML protected void showSettings() { switchView(viewSettings); }

    // ========================================================
    // ⚙️ ACTIONS DIVERSES
    // ========================================================
    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        loadDataIntoTables();
    }

    @FXML
    protected void handleSaveSettings() {
        try {
            int port = Integer.parseInt(serverPortField.getText());
            int tolerance = Integer.parseInt(toleranceField.getText());

            config.setServerPort(port);
            config.setToleranceMinutes(tolerance);
            config.saveConfig();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Paramètres sauvegardés avec succès ! \n(Le changement de port nécessite un redémarrage du serveur)");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "❌ Le port et la tolérance doivent être des nombres entiers !");
            alert.showAndWait();
        }
    }
}