package com.example.mainapp.controller.attendance;

import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.network.TCPServer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class AttendanceFormController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private Button btnDelete;

    private AttendanceRecord recordActuel;
    private boolean isCreationMode;

    @FXML
    public void initialize() {

        var company = TCPServer.getInstance().getCompany();
        if (company != null && company.getEmployees() != null) {
            employeeComboBox.setItems(FXCollections.observableArrayList(company.getEmployees()));
        }

        typeComboBox.setItems(FXCollections.observableArrayList("Entrée", "Sortie"));
    }


    public void setAttendanceRecord(AttendanceRecord record) {
        if (record == null) {
            this.isCreationMode = true;
            this.recordActuel = null;

            titleLabel.setText("Nouveau Pointage");
            btnDelete.setVisible(false);

            datePicker.setValue(LocalDate.now());
            heureField.setText(DateTimeFormatter.ofPattern("HH:mm").format(LocalTime.now()));
            typeComboBox.setValue("Entrée");

        } else {
            this.isCreationMode = false;
            this.recordActuel = record;

            titleLabel.setText("Éditer Pointage");
            btnDelete.setVisible(true);

            employeeComboBox.setValue(record.getEmployee());
            typeComboBox.setValue(record.isCheckIn() ? "Entrée" : "Sortie");
            datePicker.setValue(record.getTime().toLocalDate());
            heureField.setText(record.getTime().toLocalTime().toString());

            employeeComboBox.setDisable(true);
        }
    }

    @FXML
    private void handleSave() {
        try {

            Employee employeSelectionne = employeeComboBox.getValue();
            if (employeSelectionne == null) {
                throw new Exception("Veuillez sélectionner un employé.");
            }

            if (datePicker.getValue() == null) {
                throw new Exception("Veuillez sélectionner une date.");
            }

            if (typeComboBox.getValue() == null) {
                throw new Exception("Veuillez sélectionner le type (Entrée/Sortie).");
            }


            LocalTime heureSaisie;
            try {
                heureSaisie = LocalTime.parse(heureField.getText().trim());
            } catch (DateTimeParseException e) {
                throw new Exception("Format d'heure invalide. Utilisez le format HH:mm (ex: 08:30).");
            }

            LocalDateTime dateHeureComplete = LocalDateTime.of(datePicker.getValue(), heureSaisie);
            boolean isCheckIn = typeComboBox.getValue().equals("Entrée");

            if (isCreationMode) {
                AttendanceRecord nouveauRecord = new AttendanceRecord(employeSelectionne, dateHeureComplete, isCheckIn);
                AttendanceService.getInstance().addAttendanceRecord(nouveauRecord);
            } else {

                recordActuel.setTime(dateHeureComplete);
                recordActuel.setCheckIn(isCheckIn);
                AttendanceService.getInstance().updateAttendanceRecord(recordActuel);
            }

            fermerFenetre();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Impossible d'enregistrer le pointage");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer ce pointage ?");
        confirm.setContentText("Voulez-vous vraiment supprimer le pointage de " +
                recordActuel.getEmployee().getName() + " à " +
                recordActuel.getTime().toLocalTime() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AttendanceService.getInstance().deleteAttendanceRecord(recordActuel);
                fermerFenetre();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Suppression impossible");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    @FXML
    private void handleCancel() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) heureField.getScene().getWindow();
        stage.close();
    }
}