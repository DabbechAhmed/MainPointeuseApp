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
    @FXML private VBox viewSettings;

    // ✅ Sous-contrôleurs injectés
    @FXML private EmployeeController viewEmployeesController;
    @FXML private DepartmentController viewDepartmentsController;
    @FXML private AttendanceController viewPointagesController;

    // ========================================================
    // 📊 ÉLÉMENTS DE L'INTERFACE (Dashboard & Paramètres)
    // ========================================================
    @FXML private Label statusLabel;
    @FXML private Label employeeCountLabel;
    @FXML private Label pointagesTodayLabel;
    @FXML private Label incidentsLabel;

    @FXML private TextField serverPortField;
    @FXML private TextField toleranceField;

    // ✅ BOUTONS DU MENU
    @FXML private Button btnDashboard;
    @FXML private Button btnEmployees;
    @FXML private Button btnDepartments;
    @FXML private Button btnPointages;
    @FXML private Button btnSettings;

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

    // ========================================================
    // 📈 LOGIQUE DU DASHBOARD
    // ========================================================
    public void rafraichirDashboard() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        if (employeeCountLabel != null) {
            employeeCountLabel.setText(String.valueOf(company.getEmployees().size()));
        }

        java.time.LocalDate aujourdhui = java.time.LocalDate.now();
        int countPointages = 0;
        int countIncidents = 0;

        if (company.getAttendanceRecords() != null) {
            for (com.example.mainapp.model.AttendanceRecord record : company.getAttendanceRecords()) {
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

    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        if (viewEmployeesController != null) viewEmployeesController.rafraichirTableau();
        if (viewDepartmentsController != null) viewDepartmentsController.rafraichirTableau();
        if (viewPointagesController != null) viewPointagesController.rafraichirTableau();

        rafraichirDashboard();
    }

    // ========================================================
    // 🧭 NAVIGATION (CORRIGÉE)
    // ========================================================

    private void setActiveButton(Button clickedButton) {
        // Sécurité ajoutée : on vérifie que les boutons ne sont pas nuls avant de changer leur style
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("active");
        if (btnEmployees != null) btnEmployees.getStyleClass().remove("active");
        if (btnDepartments != null) btnDepartments.getStyleClass().remove("active");
        if (btnPointages != null) btnPointages.getStyleClass().remove("active");
        if (btnSettings != null) btnSettings.getStyleClass().remove("active");

        if (clickedButton != null && !clickedButton.getStyleClass().contains("active")) {
            clickedButton.getStyleClass().add("active");
        }
    }

    private void switchView(VBox viewToActivate, Button activeBtn) {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewEmployees != null) viewEmployees.setVisible(false);
        if (viewDepartments != null) viewDepartments.setVisible(false);
        if (viewPointages != null) viewPointages.setVisible(false);
        if (viewSettings != null) viewSettings.setVisible(false);

        if (viewToActivate != null) viewToActivate.setVisible(true);

        setActiveButton(activeBtn);
    }

    @FXML
    protected void showDashboard() {
        rafraichirDashboard();
        switchView(viewDashboard, btnDashboard);
    }

    @FXML protected void showEmployees() { switchView(viewEmployees, btnEmployees); }
    @FXML protected void showDepartments() { switchView(viewDepartments, btnDepartments); }
    @FXML protected void showPointages() { switchView(viewPointages, btnPointages); }
    @FXML protected void showSettings() { switchView(viewSettings, btnSettings); }

    // ========================================================
    // ⚙️ ACTIONS DIVERSES
    // ========================================================
    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        rafraichirUI();
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