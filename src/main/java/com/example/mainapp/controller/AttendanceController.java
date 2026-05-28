package com.example.mainapp.controller;

import com.example.mainapp.model.AttendanceRecord;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
        colAttEmpId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployee().getId().toString())
        );

        colAttEmpNom.setCellValueFactory(cellData -> {
            String nomComplet = cellData.getValue().getEmployee().getName() + " " +
                    cellData.getValue().getEmployee().getSurname();
            return new SimpleStringProperty(nomComplet);
        });

        colAttType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isCheckIn() ? "Entrée" : "Sortie")
        );

        colAttDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTime().toLocalDate().toString())
        );
        colAttHeure.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTime().toLocalTime().toString())
        );

        colAttStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus())
        );

        // ✅ NOUVEAU : Ajouter l'écouteur de double-clic pour l'édition
        attendanceTable.setRowFactory(tv -> {
            TableRow<AttendanceRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    AttendanceRecord rowData = row.getItem();
                    ouvrirFenetreAttendance(rowData);
                }
            });
            return row;
        });
    }

    public void rafraichirTableau() {
        Company company = TCPServer.getInstance().getCompany();
        if (company != null) {
            List<AttendanceRecord> listePointages = company.getAttendanceRecords();
            if (listePointages == null) listePointages = new java.util.ArrayList<>();

            ObservableList<AttendanceRecord> attList = FXCollections.observableArrayList(listePointages);
            attendanceTable.setItems(attList);
            attendanceTable.refresh();
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

    // ========================================================
    // 🪟 GESTION DE LA FENÊTRE MODALE (NOUVEAU)
    // ========================================================

    @FXML
    protected void handleAddAttendance() {
        ouvrirFenetreAttendance(null); // Mode création
    }

    private void ouvrirFenetreAttendance(AttendanceRecord record) {
        try {
            // ⚠️ Assure-toi de créer ce fichier FXML dans tes ressources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/attendance-form.fxml"));
            Parent root = loader.load();

            AttendanceFormController controller = loader.getController();
            controller.setAttendanceRecord(record);

            Stage stage = new Stage();
            stage.setTitle(record == null ? "Nouveau Pointage" : "Éditer le Pointage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            // Rafraîchir le tableau automatiquement à la fermeture de la modale
            rafraichirTableau();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du formulaire Attendance : " + e.getMessage());
            e.printStackTrace();
        }
    }
}