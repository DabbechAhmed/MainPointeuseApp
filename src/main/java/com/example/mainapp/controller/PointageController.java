package com.example.mainapp.controller;

import com.example.dto.CheckPoint;
import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.mainapp.network.TCPServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class PointageController {

    @FXML private TableView<CheckPoint> attendanceTable;
    @FXML private TableColumn<CheckPoint, String> colAttEmpId;
    @FXML private TableColumn<CheckPoint, String> colAttEmpNom;
    @FXML private TableColumn<CheckPoint, String> colAttType;
    @FXML private TableColumn<CheckPoint, String> colAttDate;
    @FXML private TableColumn<CheckPoint, String> colAttHeure;
    @FXML private TableColumn<CheckPoint, String> colAttStatut;

    @FXML private DatePicker dateFilter;

    @FXML
    public void initialize() {
        // Lier les colonnes aux propriétés de l'objet CheckPoint
        colAttEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmployeeId().toString()));

        colAttEmpNom.setCellValueFactory(cellData -> {
            CheckPoint pointageActuel = cellData.getValue();
            Company company = TCPServer.getInstance().getCompany();
            Employee emp = company != null ? company.findEmployeeById(pointageActuel.getEmployeeId()) : null;
            return new SimpleStringProperty(emp != null ? emp.getName() + " " + emp.getSurname() : "Employé inconnu");
        });

        colAttType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isCheckIn() ? "Entrée" : "Sortie"));
        colAttDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalDate().toString()));
        colAttHeure.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toLocalTime().toString()));
        colAttStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
    }

    // Méthode appelée par le MainController pour charger les données
    public void rafraichirTableau() {
        Company company = TCPServer.getInstance().getCompany();
        if (company != null) {
            List<CheckPoint> listePointages = company.getCheckPoints();
            if (listePointages == null) listePointages = new java.util.ArrayList<>();

            ObservableList<CheckPoint> attList = FXCollections.observableArrayList(listePointages);
            attendanceTable.setItems(attList);
        }
    }

    @FXML
    protected void handleFilterAttendance() {
        System.out.println("Action : Filtrer les pointages");
        if (dateFilter.getValue() != null) {
            System.out.println("Filtrage pour la date : " + dateFilter.getValue().toString());
            // TODO: Ajouter la logique pour filtrer la liste affichée dans le tableau
        }
    }
}