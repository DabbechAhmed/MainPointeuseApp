package com.example.mainapp.controller.attendance;

import com.example.mainapp.controller.departement.DepartmentService;
import com.example.mainapp.controller.employee.EmployeeService;
import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.employee.Employee;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceController {

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colAttEmpId;
    @FXML private TableColumn<AttendanceRecord, String> colAttEmpNom;
    @FXML private TableColumn<AttendanceRecord, String> colAttType;
    @FXML private TableColumn<AttendanceRecord, String> colAttDate;
    @FXML private TableColumn<AttendanceRecord, String> colAttHeure;
    @FXML private TableColumn<AttendanceRecord, String> colAttStatut;

    @FXML private DatePicker dateFilter;

    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> employeeFilter;
    @FXML private ComboBox<String> departmentFilter;

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

        if (statusFilter != null) statusFilter.setValue("Tous les statuts");
        if (employeeFilter != null) employeeFilter.setValue("Tous les employés");
        if (departmentFilter != null) departmentFilter.setValue("Tous les départements");

        chargerFiltresEmployee();
        chargerFiltresdepartement();
    }

    private void chargerFiltresEmployee() {
        List<Employee> employees = EmployeeService.getInstance().recupererTousLesEmployes();

        ObservableList<String> employeeNames = FXCollections.observableArrayList();
        employeeNames.add("Tous les employés");

        if (employees != null) {
            employees.forEach(emp ->
                employeeNames.add(emp.getName() + " " + emp.getSurname())
            );
        }

        employeeFilter.setItems(employeeNames);
        employeeFilter.setValue("Tous les employés");
    }

    private void chargerFiltresdepartement() {
        List<Department> departments = DepartmentService.getInstance().recupererTousLesDepartements();

        ObservableList<String> departmentNames = FXCollections.observableArrayList();
        departmentNames.add("Tous les départements");

        if (departments != null) {
            departments.forEach(dept ->
                departmentNames.add(dept.getName())
            );
        }

        departmentFilter.setItems(departmentNames);
        departmentFilter.setValue("Tous les départements");
    }
    @FXML
    protected void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier CSV de pointages");

        // On limite la sélection uniquement aux fichiers d'extension .csv
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv")
        );

        // Ouverture de la boîte de dialogue système
        File selectedFile = fileChooser.showOpenDialog(attendanceTable.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Exécution de l'importation de masse via notre service
                AttendanceService.getInstance().importRecordsFromCSV(selectedFile);

                // Rafraîchissement immédiat de ton TableView JavaFX
                rafraichirTableau();

                // Notification visuelle de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'importation massive des pointages s'est déroulée avec succès !");
                alert.setTitle("Importation réussie");
                alert.setHeaderText(null);
                alert.showAndWait();

            } catch (Exception e) {
                // En cas de problème (mauvais format de date, ID inexistant...)
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du traitement du fichier CSV : " + e.getMessage());
                alert.setTitle("Échec de l'importation");
                alert.setHeaderText("Impossible de traiter les données");
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

@FXML
protected void handleClearDateFilter() {
    dateFilter.setValue(null);
    handleFilterAttendance();
}

    @FXML
    protected void handleFilterAttendance() {
        System.out.println("Action : Filtrer les pointages");

        List<AttendanceRecord> listePointages = AttendanceService.getInstance().getAllAttendanceRecords();

        if (dateFilter.getValue() != null) {
            listePointages = listePointages.stream()
                .filter(r -> r.getTime().toLocalDate().equals(dateFilter.getValue()))
                .collect(Collectors.toList());
        }

        String selectedEmployee = employeeFilter.getValue();
        if (selectedEmployee != null && !selectedEmployee.equals("Tous les employés")) {
            listePointages = listePointages.stream()
                .filter(r -> {
                    String nomComplet = r.getEmployee().getName() + " " + r.getEmployee().getSurname();
                    return nomComplet.equals(selectedEmployee);
                })
                .collect(Collectors.toList());
        }

        String selectedDepartment = departmentFilter.getValue();
        if (selectedDepartment != null && !selectedDepartment.equals("Tous les départements")) {
            listePointages = listePointages.stream()
                .filter(r -> r.getEmployee().getDepartment() != null &&
                        r.getEmployee().getDepartment().getName().equals(selectedDepartment))
                .collect(Collectors.toList());
        }

        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tous les statuts")) {
            listePointages = listePointages.stream()
                .filter(r -> r.getStatus().contains(selectedStatus))
                .collect(Collectors.toList());
        }

        ObservableList<AttendanceRecord> attList = FXCollections.observableArrayList(listePointages);
        attendanceTable.setItems(attList);
    }

    public void rafraichirTableau() {
        List<AttendanceRecord> listePointages = AttendanceService.getInstance().getAllAttendanceRecords();

        if (listePointages == null) {
            listePointages = new java.util.ArrayList<>();
        }

        ObservableList<AttendanceRecord> attList = FXCollections.observableArrayList(listePointages);
        attendanceTable.setItems(attList);
        attendanceTable.refresh();
        
        chargerFiltresEmployee();
        chargerFiltresdepartement();
    }

    


    @FXML
    protected void handleAddAttendance() {
        ouvrirFenetreAttendance(null);
    }

    private void ouvrirFenetreAttendance(AttendanceRecord record) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/attendance/attendance-form.fxml"));
            Parent root = loader.load();

            AttendanceFormController controller = loader.getController();
            controller.setAttendanceRecord(record);

            Stage stage = new Stage();
            stage.setTitle(record == null ? "Nouveau Pointage" : "Éditer le Pointage");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            rafraichirTableau();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du formulaire Attendance : " + e.getMessage());
            e.printStackTrace();
        }
    }
}