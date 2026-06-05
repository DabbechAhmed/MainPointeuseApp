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

/**
 * Contrôleur graphique gérant le formulaire modal de création et d'édition d'un pointage manuel.
 * <p>
 * Cette classe permet aux administrateurs ou RH de corriger des anomalies ou d'ajouter manuellement un
 * pointage d'entrée ou de sortie pour un employé. Elle bascule dynamiquement ses composants selon le mode
 * (Création ou Édition), assure la validation stricte des formats temporels saisis (HH:mm) et communique
 * avec la couche métier {@link AttendanceService} pour recalculer immédiatement le solde de l'employé.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class AttendanceFormController {

    @FXML /** Titre dynamique de la fenêtre modale (ex: "Nouveau Pointage" ou "Éditer Pointage"). */
    private Label titleLabel;

    @FXML /** Menu déroulant de sélection de l'employé concerné par le pointage. */
    private ComboBox<Employee> employeeComboBox;

    @FXML /** Menu déroulant de sélection du type de flux (Entrée ou Sortie). */
    private ComboBox<String> typeComboBox;

    @FXML /** Sélecteur de date graphique pour le pointage. */
    private DatePicker datePicker;

    @FXML /** Champ de saisie textuel pour l'heure précise (format attendu HH:mm). */
    private TextField heureField;

    @FXML /** Bouton de suppression, rendu visible uniquement lors de l'édition d'un pointage existant. */
    private Button btnDelete;

    /** L'instance de l'enregistrement de pointage actuellement manipulée au sein du formulaire. */
    private AttendanceRecord currentRecord;

    /** Drapeau logique indiquant si le formulaire est ouvert en mode création (vrai) ou édition (faux). */
    private boolean isCreationMode;

    /**
     * Initialise les composants graphiques au chargement du FXML.
     * <p>
     * Alimente le menu déroulant des employés en extrayant la liste de l'entreprise via le serveur TCP,
     * et pré-configure les options statiques du menu de type ("Entrée" / "Sortie").
     * </p>
     */
    @FXML
    public void initialize() {
        var company = TCPServer.getInstance().getCompany();
        if (company != null && company.getEmployees() != null) {
            employeeComboBox.setItems(FXCollections.observableArrayList(company.getEmployees()));
        }

        typeComboBox.setItems(FXCollections.observableArrayList("Entrée", "Sortie"));
    }

    /**
     * Injecte le pointage à traiter et configure l'état d'affichage des composants de l'IHM.
     * <p>
     * Si l'objet fourni est {@code null}, le contrôleur s'initialise en mode création : les champs prennent
     * les valeurs temporelles de l'instant présent (Date du jour, Heure courante) et le bouton de suppression est masqué.
     * En mode édition, les champs sont figés sur les données historiques de l'enregistrement et la ComboBox
     * de l'employé est désactivée pour interdire la réaffectation d'un pointage à un autre collaborateur.
     * </p>
     *
     * @param record L'enregistrement {@link AttendanceRecord} à éditer, ou {@code null} pour un nouvel ajout.
     */
    public void setAttendanceRecord(AttendanceRecord record) {
        if (record == null) {
            this.isCreationMode = true;
            this.currentRecord = null;

            titleLabel.setText("Nouveau Pointage");
            btnDelete.setVisible(false);

            datePicker.setValue(LocalDate.now());
            heureField.setText(DateTimeFormatter.ofPattern("HH:mm").format(LocalTime.now()));
            typeComboBox.setValue("Entrée");

        } else {
            this.isCreationMode = false;
            this.currentRecord = record;

            titleLabel.setText("Éditer Pointage");
            btnDelete.setVisible(true);

            employeeComboBox.setValue(record.getEmployee());
            typeComboBox.setValue(record.isCheckIn() ? "Entrée" : "Sortie");
            datePicker.setValue(record.getTime().toLocalDate());
            heureField.setText(record.getTime().toLocalTime().toString());

            employeeComboBox.setDisable(true);
        }
    }

    /**
     * Action FXML qui valide et applique l'enregistrement ou la mise à jour du pointage manuel.
     * <p>
     * Cette méthode opère des vérifications de surface (champs non sélectionnés) puis tente de parser
     * l'heure au format standardisé ISO via {@link LocalTime#parse(CharSequence)}. Elle fusionne ensuite
     * la date et l'heure en un objet {@link LocalDateTime}. Selon le mode actif, elle sollicite
     * le {@link AttendanceService} pour insérer ou écraser le pointage, puis ferme la fenêtre modale.
     * </p>
     */
    @FXML
    private void handleSave() {
        try {
            Employee selectedEmployee = employeeComboBox.getValue();
            if (selectedEmployee == null) {
                throw new Exception("Veuillez sélectionner un employé.");
            }

            if (datePicker.getValue() == null) {
                throw new Exception("Veuillez sélectionner une date.");
            }

            if (typeComboBox.getValue() == null) {
                throw new Exception("Veuillez sélectionner le type (Entrée/Sortie).");
            }

            LocalTime parsedTime;
            try {
                parsedTime = LocalTime.parse(heureField.getText().trim());
            } catch (DateTimeParseException e) {
                throw new Exception("Format d'heure invalide. Utilisez le format HH:mm (ex: 08:30).");
            }

            LocalDateTime fullDateTime = LocalDateTime.of(datePicker.getValue(), parsedTime);
            boolean isCheckIn = typeComboBox.getValue().equals("Entrée");

            if (isCreationMode) {
                AttendanceRecord newRecord = new AttendanceRecord(selectedEmployee, fullDateTime, isCheckIn);
                AttendanceService.getInstance().addAttendanceRecord(newRecord);
            } else {
                currentRecord.setTime(fullDateTime);
                currentRecord.setCheckIn(isCheckIn);
                AttendanceService.getInstance().updateAttendanceRecord(currentRecord);
            }

            closeWindow();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Impossible d'enregistrer le pointage");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Action FXML déclenchant la suppression définitive de l'enregistrement de pointage affiché.
     * <p>
     * Déploie une boîte de dialogue de confirmation modale. Si l'administrateur valide, l'objet est
     * retiré du modèle de données global via le {@link AttendanceService}, ce qui automatise la
     * régularisation immédiate du solde de minutes de l'employé lésé.
     * </p>
     */
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer ce pointage ?");
        confirm.setContentText("Voulez-vous vraiment supprimer le pointage de " +
                currentRecord.getEmployee().getName() + " à " +
                currentRecord.getTime().toLocalTime() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AttendanceService.getInstance().deleteAttendanceRecord(currentRecord);
                closeWindow();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Suppression impossible");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    /**
     * Action FXML rattachée au bouton d'annulation de la saisie.
     * <p>
     * Interrompt l'action de modification ou d'ajout en cours et ordonne la fermeture de la fenêtre.
     * </p>
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Ferme la fenêtre graphique modale active.
     * <p>
     * Récupère dynamiquement l'arborescence de la scène (Stage) à partir du composant {@code heureField}
     * pour appeler sa fermeture propre.
     * </p>
     */
    private void closeWindow() {
        Stage stage = (Stage) heureField.getScene().getWindow();
        stage.close();
    }
}