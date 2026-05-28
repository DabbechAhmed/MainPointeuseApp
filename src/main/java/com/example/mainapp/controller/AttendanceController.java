package com.example.mainapp.controller;

import com.example.mainapp.model.AttendanceRecord;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class AttendanceController {

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colAttEmpId;
    @FXML private TableColumn<AttendanceRecord, String> colAttEmpNom;
    @FXML private TableColumn<AttendanceRecord, String> colAttType;
    @FXML private TableColumn<AttendanceRecord, String> colAttDate;
    @FXML private TableColumn<AttendanceRecord, String> colAttHeure;
    @FXML private TableColumn<AttendanceRecord, String> colAttStatut;

    @FXML private DatePicker dateFilter;

    @FXML
    public void initialize() {
        // 1. ID de l'employé : On accède à l'ID directement via l'objet Employee
        colAttEmpId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployee().getId().toString())
        );

        // 2. Nom de l'employé : Plus besoin de chercher dans Company, c'est instantané !
        colAttEmpNom.setCellValueFactory(cellData -> {
            String nomComplet = cellData.getValue().getEmployee().getName() + " " +
                    cellData.getValue().getEmployee().getSurname();
            return new SimpleStringProperty(nomComplet);
        });

        // 3. Type de pointage
        colAttType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isCheckIn() ? "Entrée" : "Sortie")
        );

        // 4. Date et Heure
        colAttDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTime().toLocalDate().toString())
        );
        colAttHeure.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTime().toLocalTime().toString())
        );

        // 5. Statut (Attention, dans le nouveau modèle c'est 'getStatus()' avec un 's')
        colAttStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
        );
    }

    // Méthode appelée par le MainController pour charger les données
    public void rafraichirTableau() {
        Company company = TCPServer.getInstance().getCompany();
        if (company != null) {
            List<AttendanceRecord> listePointages = company.getAttendanceRecords();
            if (listePointages == null) listePointages = new java.util.ArrayList<>();

            ObservableList<AttendanceRecord> attList = FXCollections.observableArrayList(listePointages);
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