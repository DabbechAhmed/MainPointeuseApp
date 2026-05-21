package com.example.mainapp.controller;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.dto.CheckPoint;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.service.PersistenceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class MainController {

    // ✅ 1. DÉCLARATION DU PONT STATIQUE : Accessible de partout dans l'app
    public static MainController instance;

    // Déclaration typée des tables et des colonnes (Employés)
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpNom;
    @FXML private TableColumn<Employee, String> colEmpPrenom;

    // Déclaration typée des tables et des colonnes (Pointages)
    @FXML private TableView<CheckPoint> attendanceTable;
    @FXML private TableColumn<CheckPoint, String> colAttEmpId;
    @FXML private TableColumn<CheckPoint, String> colAttEmpNom;
    @FXML private TableColumn<CheckPoint, String> colAttType;
    @FXML private TableColumn<CheckPoint, String> colAttDate;
    @FXML private TableColumn<CheckPoint, String> colAttHeure;

    // Avertissements normaux
    @FXML private TextField searchField;
    @FXML private DatePicker dateFilter;
    @FXML private Label statusLabel;
    @FXML private Label employeeCountLabel;

    private Company company;

    @FXML
    public void initialize() {
        // ✅ 2. INITIALISATION DU PONT : Dès que la fenêtre s'ouvre, elle s'enregistre ici
        instance = this;

        statusLabel.setText("Application démarrée");

        // Lier les colonnes des employés
        colEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        colEmpNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colEmpPrenom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSurname()));

        // Lier les colonnes des pointages
        colAttEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeId().toString()));

        // Jointure dynamique
        colAttEmpNom.setCellValueFactory(cellData -> {
            CheckPoint pointageActuel = cellData.getValue();
            Employee emp = company.findEmployeeById(pointageActuel.getEmployeeId());
            if (emp != null) {
                return new SimpleStringProperty(emp.getName() + " " + emp.getSurname());
            } else {
                return new SimpleStringProperty("Employé inconnu");
            }
        });

        colAttType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isCheckIn() ? "Entrée" : "Sortie"));
        colAttDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalDate().toString()));
        colAttHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalTime().toString()));

        // Charger les données initiales
        loadDataIntoTables();
    }

    // ✅ 3. MÉTHODE PUBLIQUE POUR LE SERVEUR : Le ClientHandler appellera cette méthode
    public void rafraichirUI() {
        loadDataIntoTables();
    }

    // Charge les données en mémoire et met à jour l'UI
    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();

        if (this.company == null) return;

        ObservableList<Employee> empList = FXCollections.observableArrayList(company.getEmployees());
        employeeTable.setItems(empList);
        employeeCountLabel.setText("Employés: " + empList.size());

        List<CheckPoint> listePointages = company.getCheckPoints();
        if (listePointages == null) {
            listePointages = new java.util.ArrayList<>();
        }
        ObservableList<CheckPoint> attList = FXCollections.observableArrayList(listePointages);
        attendanceTable.setItems(attList);
    }

    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        loadDataIntoTables();
        System.out.println("Données rafraîchies");
    }

    @FXML protected void handleAddEmployee() { System.out.println("Add Employee clicked"); }
    @FXML protected void handleDeleteEmployee() { System.out.println("Delete Employee clicked"); }
    @FXML protected void handleEditEmployee() { System.out.println("Edit Employee clicked"); }
    @FXML protected void handleFilterAttendance() { System.out.println("Filter Attendance clicked"); }
    @FXML protected void handleExit() { System.exit(0); }
}