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

import java.util.List;

public class MainController {

    public static MainController instance;
    private Company company;

    // ========================================================
    // 🔀 GESTION DES PAGES (VUES FXML)
    // ========================================================
    @FXML private VBox viewDashboard;
    @FXML private VBox viewEmployees;
    @FXML private VBox viewPointages;

    // ✅ NOUVEAU : La magie de l'injection !
    // JavaFX va automatiquement lier cette variable au contrôleur de ta page employé.
    // Le nom DOIT être l'id de ton fx:include ("viewEmployees") + "Controller"
    @FXML private EmployeeController viewEmployeesController;

    // ========================================================
    // 📊 ÉLÉMENTS DE L'INTERFACE (Dashboard et Pointages uniquement)
    // ========================================================
    // ❌ Les variables employeeTable, colEmpId, colEmpNom, etc... ONT ÉTÉ SUPPRIMÉES !

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

    // ========================================================
    // 🚀 INITIALISATION
    // ========================================================
    @FXML
    public void initialize() {
        instance = this;
        statusLabel.setText("Application démarrée");



        // Lier les colonnes des pointages
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

    // ========================================================
    // 🔄 MÉTHODES DE CHARGEMENT ET RÉSEAU
    // ========================================================
    public void rafraichirUI() {
        loadDataIntoTables();
    }

    private void loadDataIntoTables() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) return;

        // ✅ NOUVEAU : On délègue la mise à jour des employés au sous-contrôleur !
        // Le MainController ne s'occupe plus de charger les employés, il donne juste l'ordre.
        if (viewEmployeesController != null) {
            viewEmployeesController.rafraichirTableau();
        }

        // Remplir le tableau des pointages
        List<CheckPoint> listePointages = company.getCheckPoints();
        if (listePointages == null) listePointages = new java.util.ArrayList<>();
        ObservableList<CheckPoint> attList = FXCollections.observableArrayList(listePointages);
        attendanceTable.setItems(attList);

        // Mise à jour de la carte statistique du Dashboard
        employeeCountLabel.setText(String.valueOf(company.getEmployees().size()));
    }

    // ========================================================
    // 🧭 NAVIGATION (MENU LATÉRAL) - INCHANGÉ
    // ========================================================
    private void cacherToutesLesVues() {
        if (viewDashboard != null) viewDashboard.setVisible(false);
        if (viewEmployees != null) viewEmployees.setVisible(false);
        if (viewPointages != null) viewPointages.setVisible(false);
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

    @FXML
    protected void showPointages() {
        cacherToutesLesVues();
        if (viewPointages != null) viewPointages.setVisible(true);
    }

    // ========================================================
    // 🖱️ ACTIONS DES BOUTONS
    // ========================================================
    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        loadDataIntoTables();
    }


    @FXML
    protected void handleFilterAttendance() {
        System.out.println("Action : Filtrer les pointages");
    }
}