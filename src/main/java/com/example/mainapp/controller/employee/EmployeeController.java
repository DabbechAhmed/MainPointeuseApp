package com.example.mainapp.controller.employee;

import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.employee.Employee;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur graphique gérant l'interface visuelle de la liste des employés.
 * <p>
 * Cette classe est responsable de l'affichage de la table des employés, de la gestion
 * du champ de recherche dynamique pour le filtrage en temps réel, et de l'interception
 * des double-clics sur les lignes pour ouvrir le formulaire de modification en mode modal.
 * Elle communique directement avec la couche de services métier {@link EmployeeService}.
 * </p>
 * * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class EmployeeController {

    @FXML /** Composant de tableau JavaFX affichant la liste des employés. */
    private TableView<Employee> employeeTable;

    @FXML /** Colonne du tableau affichant l'identifiant unique (UUID) de l'employé. */
    private TableColumn<Employee, String> colEmpId;

    @FXML /** Colonne du tableau affichant le prénom de l'employé. */
    private TableColumn<Employee, String> colEmpNom;

    @FXML /** Colonne du tableau affichant le nom de famille de l'employé. */
    private TableColumn<Employee, String> colEmpPrenom;

    @FXML /** Colonne du tableau affichant le nom du département d'affectation. */
    private TableColumn<Employee, String> colEmpDept;

    @FXML /** Colonne du tableau affichant le statut ou rôle de l'employé. */
    private TableColumn<Employee, String> colEmpStatus;

    @FXML /** Colonne du tableau affichant le solde cumulé de minutes de travail. */
    private TableColumn<Employee, String> colEmpSolde;

    @FXML /** Champ de saisie textuel utilisé pour filtrer dynamiquement les employés de la table. */
    private TextField searchField;

    /** Liste observable de référence contenant l'intégralité des employés non filtrés pour le Data Binding. */
    private ObservableList<Employee> allEmployees;

    /**
     * Méthode d'initialisation automatique appelée par le cycle de vie JavaFX.
     * <p>
     * Configure les fabriques de cellules (CellValueFactory) pour lier chaque colonne aux propriétés
     * de l'objet {@link Employee}. Elle met en place une fabrique de lignes (RowFactory) pour écouter
     * les double-clics de l'utilisateur, et enregistre un écouteur (Listener) sur le champ de recherche
     * pour déclencher le filtrage instantané à chaque saisie d'un caractère.
     * </p>
     */
    @FXML
    public void initialize() {
        colEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        colEmpNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colEmpPrenom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSurname()));

        colEmpDept.setCellValueFactory(cellData -> {
            Department dept = cellData.getValue().getDepartment();
            return new SimpleStringProperty(dept != null ? dept.getName() : "Non assigné");
        });

        colEmpStatus.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : "N/A");
        });

        colEmpSolde.setCellValueFactory(cellData -> {
            long solde = cellData.getValue().getSoldeMinutes();
            return new SimpleStringProperty(solde + " min");
        });

        employeeTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Employee rowData = row.getItem();
                    ouvrirFenetreEmploye(rowData);
                }
            });
            return row;
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filtrerEmployes(newValue));
    }

    /**
     * Charge et affiche la boîte de dialogue modale du formulaire employé.
     * <p>
     * Cette méthode instancie le fichier FXML du formulaire, récupère son sous-contrôleur
     * {@link EmployeeFormController} pour lui injecter l'objet employé sélectionné (ou {@code null}
     * pour une création), puis bloque l'interface principale jusqu'à la fermeture de la fenêtre modale.
     * Un rafraîchissement automatique de la table est opéré dès la fermeture.
     * </p>
     *
     * @param employe L'instance d'{@link Employee} à modifier, ou {@code null} s'il s'agit d'une création.
     */
    private void ouvrirFenetreEmploye(Employee employe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/employee/employee-form.fxml"));
            Parent root = loader.load();

            EmployeeFormController controller = loader.getController();
            controller.setEmployee(employe);

            Stage stage = new Stage();
            stage.setTitle(employe == null ? "Ajouter Employé" : "Éditer Employé");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshTable();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du formulaire employé.");
        }
    }

    /**
     * Interroge le service métier pour récupérer la liste à jour des employés et rafraîchit la table graphique.
     * <p>
     * Vide également le champ de recherche textuel pour réinitialiser les filtres visuels de l'utilisateur.
     * </p>
     */
    public void refreshTable() {
        List<Employee> listEmployee = EmployeeService.getInstance().getAllEmployees();

        if (listEmployee == null) {
            listEmployee = new ArrayList<>();
        }

        allEmployees = FXCollections.observableArrayList(listEmployee);
        employeeTable.setItems(allEmployees);
        employeeTable.refresh();

        searchField.clear();
    }

    /**
     * Filtre en temps réel les éléments affichés dans le tableau selon les critères de saisie.
     * <p>
     * La recherche est insensible à la casse (case-insensitive) et analyse simultanément si la chaîne
     * correspond à tout ou partie de l'UUID, du nom, du prénom ou du département de l'employé.
     * </p>
     *
     * @param searchTerm La chaîne de caractères saisie par l'utilisateur dans le champ de recherche.
     */
    private void filtrerEmployes(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            employeeTable.setItems(allEmployees);
            return;
        }

        String searchLower = searchTerm.toLowerCase();

        List<Employee> employeesFiltres = allEmployees.stream()
                .filter(emp -> emp.getId().toString().toLowerCase().contains(searchLower)
                        || emp.getName().toLowerCase().contains(searchLower)
                        || emp.getSurname().toLowerCase().contains(searchLower)
                        || (emp.getDepartment() != null && emp.getDepartment().getName().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());

        employeeTable.setItems(FXCollections.observableArrayList(employeesFiltres));
    }

    /**
     * Action FXML déclenchée lors du clic sur le bouton d'ajout d'un nouvel employé.
     * <p>
     * Délègue l'action à la méthode {@link #ouvrirFenetreEmploye(Employee)} en lui passant {@code null}.
     * </p>
     */
    @FXML
    protected void handleAddEmployee() {
        ouvrirFenetreEmploye(null);
    }
}