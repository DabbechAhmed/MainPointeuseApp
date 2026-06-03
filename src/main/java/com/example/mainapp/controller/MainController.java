package com.example.mainapp.controller;

import com.example.mainapp.controller.attendance.AttendanceController;
import com.example.mainapp.controller.departement.DepartmentController;
import com.example.mainapp.controller.employee.EmployeeController;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class MainController {

    public static MainController instance;
    private Company company;

    @FXML private VBox viewDashboard;
    @FXML private VBox viewEmployees;
    @FXML private VBox viewDepartments;
    @FXML private VBox viewPointages;
    @FXML private VBox viewSettings;

    @FXML private EmployeeController viewEmployeesController;
    @FXML private DepartmentController viewDepartmentsController;
    @FXML private AttendanceController viewPointagesController;
    @FXML private SettingsController viewSettingsController;

    @FXML private Label statusLabel;
    @FXML private Label employeeCountLabel;
    @FXML private Label pointagesTodayLabel;
    @FXML private Label incidentsLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnEmployees;
    @FXML private Button btnDepartments;
    @FXML private Button btnPointages;
    @FXML private Button btnSettings;

    @FXML
    public void initialize() {
        instance = this;
        statusLabel.setText("Application démarrée");

        loadDataIntoTables();
        showDashboard();
    }

    public void rafraichirUI() {
        loadDataIntoTables();
    }

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

    @FXML
    protected void showEmployees() {
        switchView(viewEmployees, btnEmployees);
    }

    @FXML
    protected void showDepartments() {
        switchView(viewDepartments, btnDepartments);
    }

    @FXML
    protected void showPointages() {
        switchView(viewPointages, btnPointages);
    }

    @FXML
    protected void showSettings() {
        switchView(viewSettings, btnSettings);
    }

    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        rafraichirUI();
    }

    public void cleanup() {
        instance = null;
    }

}