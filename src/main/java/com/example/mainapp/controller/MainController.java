package com.example.mainapp.controller;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.dto.CheckPoint;
import com.example.mainapp.network.TCPServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import com.example.mainapp.utils.ConfigManager;
import java.util.List;

public class MainController {

    public static MainController instance;
    private Company company;

    // ========================================================
    // 🔀 GESTION DES PAGES (VUES FXML)
    // ========================================================
    @FXML private VBox viewDashboard;
    @FXML private VBox viewEmployees;
    @FXML private VBox viewDepartments; // ✅ NOUVEAU
    @FXML private VBox viewPointages;

    @FXML private EmployeeController viewEmployeesController;
    @FXML private DepartmentController viewDepartmentsController;


    // ✅ NOUVEAU

    // ========================================================
    // 📊 ÉLÉMENTS DE L'INTERFACE (Pointages uniquement)
    // ========================================================
    @FXML private TableView<CheckPoint> attendanceTable;
    @FXML private TableColumn<CheckPoint, String> colAttEmpId;
    @FXML private TableColumn<CheckPoint, String> colAttEmpNom;
    @FXML private TableColumn<CheckPoint, String> colAttType;
    @FXML private TableColumn<CheckPoint, String> colAttDate;
    @FXML private TableColumn<CheckPoint, String> colAttHeure;
    @FXML private TableColumn<CheckPoint, String> colAttStatut;

    @FXML private DatePicker dateFilter;
    @FXML private Label statusLabel;
    @FXML private Label employeeCountLabel;

    @FXML private VBox viewSettings;
    @FXML private TextField serverPortField;
    @FXML private TextField toleranceField;

    private ConfigManager config;

    @FXML
    public void initialize() {
        instance = this;
        statusLabel.setText("Application démarrée");
        this.config = new ConfigManager();
        serverPortField.setText(String.valueOf(config.getServerPort()));
        toleranceField.setText(String.valueOf(config.getToleranceMinutes()));

        colAttEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeId().toString()));
        colAttEmpNom.setCellValueFactory(cellData -> {
            CheckPoint pointageActuel = cellData.getValue();
            Employee emp = company != null ? company.findEmployeeById(pointageActuel.getEmployeeId()) : null;
            return new SimpleStringProperty(emp != null ? emp.getName() + " " + emp.getSurname() : "Employé inconnu");
        });
        colAttType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isCheckIn() ? "Entrée" : "Sortie"));
        colAttDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalDate().toString()));
        colAttHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalTime().toString()));
        colAttStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));

        loadDataIntoTables();
        showDashboard();
    }

    public void rafraichirUI() {
        loadDataIntoTables();
    }

    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        if (viewEmployeesController != null) {
            viewEmployeesController.rafraichirTableau();
        }

        // ✅ NOUVEAU : On demande au sous-contrôleur Département de se rafraîchir
        if (viewDepartmentsController != null) {
            viewDepartmentsController.rafraichirTableau();
        }

        List<CheckPoint> listePointages = company.getCheckPoints();
        if (listePointages == null) listePointages = new java.util.ArrayList<>();
        ObservableList<CheckPoint> attList = FXCollections.observableArrayList(listePointages);
        attendanceTable.setItems(attList);

        employeeCountLabel.setText(String.valueOf(company.getEmployees().size()));
    }

    // ========================================================
    // 🧭 NAVIGATION
    // ========================================================
    private void cacherToutesLesVues() {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewEmployees != null) viewEmployees.setVisible(false);
        if (viewDepartments != null) viewDepartments.setVisible(false);
        if (viewPointages != null) viewPointages.setVisible(false);
        if (viewSettings != null) viewSettings.setVisible(false);
    }

    @FXML
    protected void showDashboard() {
        cacherToutesLesVues();
        if (viewDashboard != null) viewDashboard.setVisible(true);
    }

    @FXML
    protected void showEmployees() {
        cacherToutesLesVues();
        if (viewEmployees != null) viewEmployees.setVisible(true);
    }

    // ✅ NOUVELLE MÉTHODE DE NAVIGATION
    @FXML
    protected void showDepartments() {
        cacherToutesLesVues();
        if (viewDepartments != null) viewDepartments.setVisible(true);
    }

    @FXML
    protected void showPointages() {
        cacherToutesLesVues();
        if (viewPointages != null) viewPointages.setVisible(true);
    }

    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        loadDataIntoTables();
    }

    @FXML
    protected void handleFilterAttendance() {
        System.out.println("Action : Filtrer les pointages");
    }
    @FXML
    protected void showSettings() {
        cacherToutesLesVues(); // On cache proprement tout le reste
        if (viewSettings != null) viewSettings.setVisible(true); // On affiche les paramètres
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