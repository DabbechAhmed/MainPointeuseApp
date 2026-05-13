package com.example.mainapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MainController {

    @FXML
    private TableView<?> employeeTable;

    @FXML
    private TableView<?> attendanceTable;

    @FXML
    private TableView<?> scheduleTable;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker dateFilter;

    @FXML
    private Label statusLabel;

    @FXML
    private Label employeeCountLabel;

    @FXML
    public void initialize() {
        statusLabel.setText("Application démarrée");
        employeeCountLabel.setText("Employés: 0");
    }

    @FXML
    protected void handleAddEmployee() {
        statusLabel.setText("Ajouter un employé...");
        System.out.println("Add Employee clicked");
    }

    @FXML
    protected void handleDeleteEmployee() {
        statusLabel.setText("Supprimer un employé...");
        System.out.println("Delete Employee clicked");
    }

    @FXML
    protected void handleEditEmployee() {
        statusLabel.setText("Modifier un employé...");
        System.out.println("Edit Employee clicked");
    }

    @FXML
    protected void handleRefresh() {
        statusLabel.setText("Rafraîchissement des données...");
        System.out.println("Refresh clicked");
    }

    @FXML
    protected void handleFilterAttendance() {
        statusLabel.setText("Filtrage des pointages...");
        System.out.println("Filter Attendance clicked");
    }

    @FXML
    protected void handleExit() {
        System.exit(0);
    }
}