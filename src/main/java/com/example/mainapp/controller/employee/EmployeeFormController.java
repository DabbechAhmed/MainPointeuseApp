package com.example.mainapp.controller.employee;

import com.example.mainapp.model.employee.Status;
import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.model.schedule.Schedule;
import com.example.mainapp.model.schedule.TimeSlot;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import com.example.mainapp.network.TCPServer;

/**
 * Contrôleur graphique gérant le formulaire modal de création et d'édition d'un employé.
 * <p>
 * Cette classe prend en charge la double casquette du formulaire (Ajout ou Modification).
 * Elle pré-remplit les informations d'identité, charge dynamiquement les départements disponibles
 * depuis le modèle central, gère l'affichage contextuel du bouton de suppression, et assure
 * le parsing et la validation textuelle stricte de la grille des horaires hebdomadaires.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class EmployeeFormController {

    @FXML /** Titre dynamique de la fenêtre modale (ex: "Nouvel Employé" ou "Éditer l'employé"). */
    private Label titleLabel;

    @FXML /** Champ de saisie textuel pour le prénom de l'employé. */
    private TextField nomField;

    @FXML /** Champ de saisie textuel pour le nom de famille de l'employé. */
    private TextField prenomField;

    @FXML /** Menu déroulant de sélection du département d'affectation. */
    private ComboBox<Department> deptComboBox;

    @FXML /** Menu déroulant de sélection du statut/rôle (ex: EMP, RH). */
    private ComboBox<Status> statusComboBox;

    @FXML /** Bouton de suppression, visible uniquement en mode édition. */
    private Button btnDelete;

    @FXML /** Champ de saisie des horaires pour le lundi. */
    private TextField lundiField;

    @FXML /** Champ de saisie des horaires pour le mardi. */
    private TextField mardiField;

    @FXML /** Champ de saisie des horaires pour le mercredi. */
    private TextField mercrediField;

    @FXML /** Champ de saisie des horaires pour le jeudi. */
    private TextField jeudiField;

    @FXML /** Champ de saisie des horaires pour le vendredi. */
    private TextField vendrediField;

    /** L'instance d'employé en cours de traitement (création ou modification). */
    private Employee currentEmployee;

    /** Drapeau logique indiquant si le formulaire est ouvert en mode création (vrai) ou édition (faux). */
    private boolean isCreationMode;

    /**
     * Initialise les composants graphiques au chargement du FXML.
     * <p>
     * Remplis la ComboBox des statuts avec les valeurs de l'énumération {@link Status}
     * et récupère les départements de l'entreprise via le serveur TCP pour alimenter le menu déroulant.
     * </p>
     */
    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(Status.values()));

        var company = TCPServer.getInstance().getCompany();
        if (company != null) {
            deptComboBox.setItems(FXCollections.observableArrayList(company.getDepartments()));
        }
    }

    /**
     * Injecte l'employé à éditer et configure le comportement visuel du formulaire.
     * <p>
     * Si l'employé fourni est {@code null}, le contrôleur bascule automatiquement en mode création,
     * génère une instance vierge et masque le bouton de suppression. Sinon, il passe en mode édition
     * et pré-remplit l'ensemble des champs d'identité et des plannings horaires.
     * </p>
     *
     * @param employee L'objet {@link Employee} à modifier, ou {@code null} pour une création.
     */
    public void setEmployee(Employee employee) {
        if (employee == null) {
            this.isCreationMode = true;
            this.currentEmployee = new Employee();
            titleLabel.setText("Nouvel Employé");
            btnDelete.setVisible(false);
            fillScheduleFields(this.currentEmployee.getSchedule());
        } else {
            this.isCreationMode = false;
            this.currentEmployee = employee;
            titleLabel.setText("Éditer l'employé");
            btnDelete.setVisible(true);

            nomField.setText(employee.getName());
            prenomField.setText(employee.getSurname());
            deptComboBox.setValue(employee.getDepartment());
            statusComboBox.setValue(employee.getStatus());
            fillScheduleFields(employee.getSchedule());
        }
    }

    /**
     * Extrait les grilles de plages horaires du planning pour alimenter textuellement les champs de l'IHM.
     *
     * @param schedule L'objet {@link Schedule} contenant les horaires théoriques de l'employé.
     */
    private void fillScheduleFields(Schedule schedule) {
        if (schedule != null) {
            lundiField.setText(schedule.getHorairePourJour(DayOfWeek.MONDAY).toString());
            mardiField.setText(schedule.getHorairePourJour(DayOfWeek.TUESDAY).toString());
            mercrediField.setText(schedule.getHorairePourJour(DayOfWeek.WEDNESDAY).toString());
            jeudiField.setText(schedule.getHorairePourJour(DayOfWeek.THURSDAY).toString());
            vendrediField.setText(schedule.getHorairePourJour(DayOfWeek.FRIDAY).toString());
        }
    }

    /**
     * Action FXML qui valide et enregistre les modifications de la fiche employé.
     * <p>
     * Cette méthode récupère les entrées textuelles, met à jour l'objet métier, convertit et valide
     * les chaînes de caractères des horaires en structures {@link TimeSlot}. En cas de succès, elle appelle
     * le service d'arrière-plan approprié (création ou mise à jour). En cas de format erroné ou d'anomalie
     * de validation, une boîte de dialogue d'alerte graphique est affichée à l'utilisateur.
     * </p>
     */
    @FXML
    private void handleSave() {
        currentEmployee.setName(nomField.getText().trim());
        currentEmployee.setSurname(prenomField.getText().trim());
        currentEmployee.setDepartment(deptComboBox.getValue());
        currentEmployee.setStatus(statusComboBox.getValue());

        Schedule schedule = currentEmployee.getSchedule();
        if (schedule == null) {
            schedule = new Schedule();
            currentEmployee.setSchedule(schedule);
        }

        try {
            schedule.definirJournee(DayOfWeek.MONDAY, convertTextToTimeSlot(lundiField.getText()));
            schedule.definirJournee(DayOfWeek.TUESDAY, convertTextToTimeSlot(mardiField.getText()));
            schedule.definirJournee(DayOfWeek.WEDNESDAY, convertTextToTimeSlot(mercrediField.getText()));
            schedule.definirJournee(DayOfWeek.THURSDAY, convertTextToTimeSlot(jeudiField.getText()));
            schedule.definirJournee(DayOfWeek.FRIDAY, convertTextToTimeSlot(vendrediField.getText()));

            if (isCreationMode) {
                EmployeeService.getInstance().createEmployee(currentEmployee);
            } else {
                EmployeeService.getInstance().updateEmployee(currentEmployee);
            }

            System.out.println("Employé et horaires enregistrés avec succès.");
            closeWindow();

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

    /**
     * Parse et convertit une chaîne de caractères brute en un objet plage horaire.
     * <p>
     * Découpe le texte fourni autour du séparateur {@code "-"}. Si le champ est vide, retourne
     * un créneau d'usine par défaut. Lève une exception si le format chronologique ou textuel est violé.
     * </p>
     *
     * @param text La chaîne textuelle à interpréter (ex: "08:30 - 16:45").
     * @return L'objet {@link TimeSlot} modélisant la plage valide calculée.
     * @throws IllegalArgumentException Se déclenche si le caractère séparateur est absent de la saisie.
     * @throws DateTimeParseException   Se déclenche si les blocs horaires ne correspondent pas au standard ISO HH:mm.
     */
    private TimeSlot convertTextToTimeSlot(String text) throws IllegalArgumentException, DateTimeParseException {
        if (text == null || text.trim().isEmpty()) {
            return new TimeSlot();
        }

        String[] parts = text.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Le séparateur '-' est manquant.");
        }

        LocalTime arrivee = LocalTime.parse(parts[0].trim());
        LocalTime depart = LocalTime.parse(parts[1].trim());
        return new TimeSlot(arrivee, depart);
    }

    /**
     * Action FXML déclenchant la suppression définitive de l'employé affiché.
     * <p>
     * Ouvre une boîte de confirmation modale (Confirmation Alert). Si l'administrateur confirme,
     * délègue la radiation définitive à la couche {@link EmployeeService} et ferme la fenêtre.
     * </p>
     */
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer définitivement cet employé ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + currentEmployee.getName() + " ? Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                EmployeeService.getInstance().deleteEmployee(currentEmployee);
                System.out.println("Employé supprimé : " + currentEmployee.getId());
                closeWindow();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Échec de la suppression");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    /**
     * Action FXML rattachée au bouton d'annulation.
     * <p>
     * Interrompt l'opération en cours et ferme la fenêtre sans sauvegarder les modifications.
     * </p>
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Ferme la fenêtre graphique modale active.
     * <p>
     * Récupère le conteneur de scène à partir du composant {@code nomField} pour ordonner la fermeture du Stage.
     * </p>
     */
    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}