package com.example.mainapp.controller.employee;

import com.example.mainapp.enums.Status;
import com.example.mainapp.model.Department;
import com.example.mainapp.model.Employee;
import com.example.mainapp.model.Schedule;
import com.example.mainapp.model.TimeSlot;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import com.example.mainapp.network.TCPServer;
public class EmployeeFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<Department> deptComboBox;
    @FXML private ComboBox<Status> statusComboBox;
    @FXML private Button btnDelete;

    @FXML private TextField lundiField;
    @FXML private TextField mardiField;
    @FXML private TextField mercrediField;
    @FXML private TextField jeudiField;
    @FXML private TextField vendrediField;

    private Employee employeeActuel;
    private boolean isCreationMode;

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(Status.values()));

        var company = TCPServer.getInstance().getCompany();
        if (company != null) {
            deptComboBox.setItems(FXCollections.observableArrayList(company.getDepartments()));
        }
    }

    public void setEmployee(Employee employee) {
        if (employee == null) {
            this.isCreationMode = true;
            this.employeeActuel = new Employee();
            titleLabel.setText("Nouvel Employé");
            btnDelete.setVisible(false);
            remplirChampsHoraires(this.employeeActuel.getSchedule());
        } else {
            this.isCreationMode = false;
            this.employeeActuel = employee;
            titleLabel.setText("Éditer l'employé");
            btnDelete.setVisible(true);

            nomField.setText(employee.getName());
            prenomField.setText(employee.getSurname());
            deptComboBox.setValue(employee.getDepartment());
            statusComboBox.setValue(employee.getStatus());
            remplirChampsHoraires(employee.getSchedule());
        }
    }

    private void remplirChampsHoraires(Schedule planning) {
        if (planning != null) {
            lundiField.setText(planning.getHorairePourJour(DayOfWeek.MONDAY).toString());
            mardiField.setText(planning.getHorairePourJour(DayOfWeek.TUESDAY).toString());
            mercrediField.setText(planning.getHorairePourJour(DayOfWeek.WEDNESDAY).toString());
            jeudiField.setText(planning.getHorairePourJour(DayOfWeek.THURSDAY).toString());
            vendrediField.setText(planning.getHorairePourJour(DayOfWeek.FRIDAY).toString());
        }
    }

    @FXML
    private void handleSave() {
        employeeActuel.setName(nomField.getText().trim());
        employeeActuel.setSurname(prenomField.getText().trim());
        employeeActuel.setDepartment(deptComboBox.getValue());
        employeeActuel.setStatus(statusComboBox.getValue());

        Schedule planning = employeeActuel.getSchedule();
        if (planning == null) {
            planning = new Schedule();
            employeeActuel.setSchedule(planning);
        }

        try {
            planning.definirJournee(DayOfWeek.MONDAY, convertirTexteEnTimeSlot(lundiField.getText()));
            planning.definirJournee(DayOfWeek.TUESDAY, convertirTexteEnTimeSlot(mardiField.getText()));
            planning.definirJournee(DayOfWeek.WEDNESDAY, convertirTexteEnTimeSlot(mercrediField.getText()));
            planning.definirJournee(DayOfWeek.THURSDAY, convertirTexteEnTimeSlot(jeudiField.getText()));
            planning.definirJournee(DayOfWeek.FRIDAY, convertirTexteEnTimeSlot(vendrediField.getText()));

            if (isCreationMode) {
                EmployeeService.getInstance().creerEmploye(employeeActuel);
            } else {
                EmployeeService.getInstance().mettreAJourEmploye(employeeActuel);
            }

            System.out.println("Employé et horaires enregistrés avec succès.");
            fermerFenetre();

        } catch (IllegalArgumentException | DateTimeParseException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Format d'horaire invalide");
            alert.setContentText("Veuillez respecter le format HH:mm - HH:mm (ex: 08:00 - 17:00).\n" + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'enregistrement");
            alert.setHeaderText("Impossible de sauvegarder l'employé");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private TimeSlot convertirTexteEnTimeSlot(String texte) throws IllegalArgumentException, DateTimeParseException {
        if (texte == null || texte.trim().isEmpty()) {
            return new TimeSlot();
        }

        String[] parts = texte.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Le séparateur '-' est manquant.");
        }

        LocalTime arrivee = LocalTime.parse(parts[0].trim());
        LocalTime depart = LocalTime.parse(parts[1].trim());

        return new TimeSlot(arrivee, depart);
    }

    @FXML
    private void handleDelete() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer définitivement cet employé ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + employeeActuel.getName() + " ? Cette action est irréversible.");

        // On attend la réponse de l'utilisateur
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Si l'utilisateur clique sur OK, on lance la suppression métier
                EmployeeService.getInstance().supprimerEmploye(employeeActuel);
                System.out.println("Employé supprimé : " + employeeActuel.getId());
                fermerFenetre();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Échec de la suppression");
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
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}