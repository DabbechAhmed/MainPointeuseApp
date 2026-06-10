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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur graphique gérant l'interface visuelle de l'historique et du filtrage des pointages.
 * <p>
 * Cette classe orchestre la visualisation de la table des pointages (entrées et sorties) des employés.
 * Elle gère un système de filtres combinatoires complexes en temps réel (recherche croisée par date,
 * par employé, par département et par statut d'incident). Elle prend également en charge le déclenchement
 * de la boîte de dialogue système pour l'importation de fichiers CSV et l'ouverture en mode modal
 * du formulaire de correction manuelle de pointage.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class AttendanceController {

    @FXML /** Composant de tableau JavaFX affichant la liste des enregistrements de pointage. */
    private TableView<AttendanceRecord> attendanceTable;

    @FXML /** Colonne du tableau affichant l'UUID de l'employé associé au pointage. */
    private TableColumn<AttendanceRecord, String> colAttEmpId;

    @FXML /** Colonne du tableau affichant le nom complet (Prénom + Nom) de l'employé. */
    private TableColumn<AttendanceRecord, String> colAttEmpNom;

    @FXML /** Colonne du tableau affichant la nature de l'action ("Entrée" ou "Sortie"). */
    private TableColumn<AttendanceRecord, String> colAttType;

    @FXML /** Colonne du tableau affichant la date calendaire du pointage. */
    private TableColumn<AttendanceRecord, String> colAttDate;

    @FXML /** Colonne du tableau affichant l'horodatage précis (heure) de l'action. */
    private TableColumn<AttendanceRecord, String> colAttHeure;

    @FXML /** Colonne du tableau affichant le statut qualificatif ou l'anomalie du pointage (ex: Retard, Doublon). */
    private TableColumn<AttendanceRecord, String> colAttStatut;

    @FXML /** Sélecteur de date graphique (DatePicker) utilisé comme critère de filtrage temporel. */
    private DatePicker dateFilter;

    @FXML /** Menu déroulant de filtrage par nature de statut (Normal, Incidents, Retards...). */
    private ComboBox<String> statusFilter;

    @FXML /** Menu déroulant de filtrage ciblé sur un employé spécifique. */
    private ComboBox<String> employeeFilter;

    @FXML /** Menu déroulant de filtrage ciblé sur un département spécifique. */
    private ComboBox<String> departmentFilter;

    /**
     * Méthode d'initialisation automatique appelée par le cycle de vie JavaFX.
     * <p>
     * Lie individuellement les colonnes graphiques aux propriétés du modèle {@link AttendanceRecord}
     * par réflexion via des {@link SimpleStringProperty}. Elle configure une fabrique de lignes
     * (RowFactory) pour capturer les double-clics afin d'éditer un pointage, initialise les libellés
     * par défaut des menus de filtrage et déclenche le chargement asynchrone des données de filtres.
     * </p>
     */
    @FXML
    public void initialize() {
        colAttEmpId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmployee().getId().toString())
        );

        colAttEmpNom.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getEmployee().getName() + " " +
                    cellData.getValue().getEmployee().getSurname();
            return new SimpleStringProperty(fullName);
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
                    openAttendanceWindow(rowData);
                }
            });
            return row;
        });

        if (statusFilter != null) statusFilter.setValue("Tous les statuts");
        if (employeeFilter != null) employeeFilter.setValue("Tous les employés");
        if (departmentFilter != null) departmentFilter.setValue("Tous les départements");

        loadEmployeeFilters();
        loadDepartmentFilters();
    }

    /**
     * Interroge la couche de service des employés pour alimenter dynamiquement le menu déroulant du filtre.
     * <p>
     * Ajoute l'option universelle "Tous les employés" en tête de liste avant d'injecter la concaténation
     * du prénom et du nom de chaque collaborateur actif.
     * </p>
     */
    private void loadEmployeeFilters() {
        List<Employee> employees = EmployeeService.getInstance().getAllEmployees();

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

    /**
     * Interroge la couche de service des départements pour alimenter dynamiquement le menu déroulant du filtre.
     * <p>
     * Ajoute l'option universelle "Tous les départements" en tête de liste avant d'injecter le libellé
     * nominatif de chaque service répertorié.
     * </p>
     */
    private void loadDepartmentFilters() {
        List<Department> departments = DepartmentService.getInstance().getAllDepartments();

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

    /**
     * Action FXML déclenchant l'ouverture de l'explorateur de fichiers pour l'importation d'un fichier CSV.
     * <p>
     * Configure un filtre d'extension restrictif (uniquement {@code *.csv}), déploie la boîte de dialogue
     * système native et transmet le fichier sélectionné à la méthode d'ingénierie lourde du {@link AttendanceService}.
     * En cas de succès ou d'anomalie de parsing, une notification graphique adaptée (Alert) est présentée à l'utilisateur.
     * </p>
     */
    @FXML
    protected void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier CSV de pointages");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(attendanceTable.getScene().getWindow());

        if (selectedFile != null) {
            try {
                AttendanceService.getInstance().importRecordsFromCSV(selectedFile);
                refreshTable();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "L'importation massive des pointages s'est déroulée avec succès !");
                alert.setTitle("Importation réussie");
                alert.setHeaderText(null);
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du traitement du fichier CSV : " + e.getMessage());
                alert.setTitle("Échec de l'importation");
                alert.setHeaderText("Impossible de traiter les données");
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    /**
     * Action FXML réinitialisant le filtre graphique de sélection de date.
     * <p>
     * Écrase la valeur du sélecteur à {@code null} et relance la chaîne de filtrage globale.
     * </p>
     */
    @FXML
    protected void handleClearDateFilter() {
        dateFilter.setValue(null);
        handleFilterAttendance();
    }

    /**
     * Action FXML exécutant le filtrage combinatoire croisé de l'historique des pointages.
     * <p>
     * Cette méthode extrait la liste brute des enregistrements, puis applique une série successive de filtres
     * par pipeline d'API Stream en évaluant l'état de chaque composant de l'IHM :
     * <ul>
     * <li><b>Filtre Date :</b> Correspondance stricte sur le jour calendaire.</li>
     * <li><b>Filtre Employé :</b> Analyse de l'identité textuelle complète.</li>
     * <li><b>Filtre Département :</b> Validation de l'affectation structurelle.</li>
     * <li><b>Filtre Statut :</b> Analyse des sous-chaînes de caractères qualificatives (ex: contient "incident").</li>
     * </ul>
     * La liste finale calculée est encapsulée dans une collection observable pour ré-alimenter le tableau.
     * </p>
     */
    @FXML
    protected void handleFilterAttendance() {
        System.out.println("Action : Filtrer les pointages");

        List<AttendanceRecord> attendanceList = AttendanceService.getInstance().getAllAttendanceRecords();

        if (dateFilter.getValue() != null) {
            attendanceList = attendanceList.stream()
                    .filter(r -> r.getTime().toLocalDate().equals(dateFilter.getValue()))
                    .collect(Collectors.toList());
        }

        String selectedEmployee = employeeFilter.getValue();
        if (selectedEmployee != null && !selectedEmployee.equals("Tous les employés")) {
            attendanceList = attendanceList.stream()
                    .filter(r -> {
                        String fullName = r.getEmployee().getName() + " " + r.getEmployee().getSurname();
                        return fullName.equals(selectedEmployee);
                    })
                    .collect(Collectors.toList());
        }

        String selectedDepartment = departmentFilter.getValue();
        if (selectedDepartment != null && !selectedDepartment.equals("Tous les départements")) {
            attendanceList = attendanceList.stream()
                    .filter(r -> r.getEmployee().getDepartment() != null &&
                            r.getEmployee().getDepartment().getName().equals(selectedDepartment))
                    .collect(Collectors.toList());
        }

        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tous les statuts")) {
            attendanceList = attendanceList.stream()
                    .filter(r -> r.getStatus().contains(selectedStatus))
                    .collect(Collectors.toList());
        }

        ObservableList<AttendanceRecord> recordObservableList = FXCollections.observableArrayList(attendanceList);
        attendanceTable.setItems(recordObservableList);
    }

    /**
     * Force le rechargement complet des données historiques et actualise le composant de tableau.
     * <p>
     * Cette méthode met à jour la table graphique et relance la synchronisation des listes d'éléments
     * au sein des ComboBox de filtrage des employés et des départements.
     * </p>
     */
    public void refreshTable() {
        loadEmployeeFilters();
        loadDepartmentFilters();
        List<AttendanceRecord> attendanceList = AttendanceService.getInstance().getAllAttendanceRecords();

        if (attendanceList == null) {
            attendanceList = new ArrayList<>();
        }

        ObservableList<AttendanceRecord> recordObservableList = FXCollections.observableArrayList(attendanceList);
        attendanceTable.setItems(recordObservableList);
        attendanceTable.refresh();


    }

    /**
     * Action FXML déclenchée lors du clic sur le bouton de création manuelle d'un pointage.
     * <p>
     * Appelle la méthode d'ouverture de formulaire modale en transmettant la valeur {@code null}.
     * </p>
     */
    @FXML
    protected void handleAddAttendance() {
        openAttendanceWindow(null);
    }

    /**
     * Charge et affiche la boîte de dialogue modale du formulaire de pointage.
     * <p>
     * Cette méthode instancie le fichier FXML du formulaire, extrait son sous-contrôleur
     * {@link AttendanceFormController} pour y injecter l'enregistrement sélectionné (ou {@code null}
     * en mode ajout manuel), configure le blocage de l'interface parente (Modality) et rafraîchit
     * automatiquement le tableau lors de la fermeture de la fenêtre modale.
     * </p>
     *
     * @param record L'enregistrement {@link AttendanceRecord} à éditer, ou {@code null} pour une création manuelle.
     */
    private void openAttendanceWindow(AttendanceRecord record) {
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
            refreshTable();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture du formulaire Attendance : " + e.getMessage());
            e.printStackTrace();
        }
    }
}