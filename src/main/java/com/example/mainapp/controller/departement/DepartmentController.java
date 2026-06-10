package com.example.mainapp.controller.departement;

import com.example.mainapp.model.department.Department;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.util.List;

/**
 * Contrôleur graphique gérant l'interface visuelle de la liste des départements.
 * <p>
 * Cette classe est responsable du pont de données (Data Binding) entre le modèle de données
 * et le tableau {@link TableView} affichant les départements. Elle calcule dynamiquement
 * les statistiques d'effectifs pour chaque ligne, intercepte les double-clics pour ouvrir
 * le formulaire d'édition en mode modal, et collabore avec la couche {@link DepartmentService}.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class DepartmentController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> colDeptId;
    @FXML private TableColumn<Department, String> colDeptNom;
    @FXML private TableColumn<Department, String> colDeptNbEmp;
    @FXML private TextField searchField;

    private final ObservableList<Department> masterData = FXCollections.observableArrayList();

    /**
     * Méthode d'initialisation automatique appelée par le cycle de vie JavaFX.
     * <p>
     * Configure les fabriques de valeurs de cellules (CellValueFactory) pour extraire l'ID,
     * le nom et mesurer la taille de la liste d'employés de chaque département.
     * Elle configure également le système de filtrage dynamique basé sur une FilteredList.
     * </p>
     */
    @FXML
    public void initialize() {
        colDeptId.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId() != null) {
                return new SimpleStringProperty(cellData.getValue().getId().toString());
            }
            return new SimpleStringProperty("N/A");
        });

        colDeptNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        colDeptNbEmp.setCellValueFactory(cellData -> {
            Department dept = cellData.getValue();
            int employeeCount = (dept.getEmployees() != null) ? dept.getEmployees().size() : 0;
            return new SimpleStringProperty(employeeCount + " employé(s)");
        });

        FilteredList<Department> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(department -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (department.getName() != null && department.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (department.getId() != null && department.getId().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        departmentTable.setItems(filteredData);

        departmentTable.setRowFactory(tv -> {
            TableRow<Department> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Department rowData = row.getItem();
                    openDepartmentWindow(rowData);
                }
            });
            return row;
        });
    }

    /**
     * Interroge le service métier pour récupérer la liste à jour des départements et rafraîchit la table graphique.
     * <p>
     * Met à jour la liste maîtresse sous-jacente sans rompre les liaisons de la FilteredList.
     * </p>
     */
    public void refreshTable() {
        List<Department> departmentList = DepartmentService.getInstance().getAllDepartments();

        if (departmentList == null) {
            departmentList = new java.util.ArrayList<>();
        }

        masterData.setAll(departmentList);
        departmentTable.refresh();
    }

    /**
     * Action FXML déclenchée lors du clic sur le bouton de création d'un département.
     * <p>
     * Appelle la méthode d'ouverture de fenêtre modale en transmettant la valeur {@code null}.
     * </p>
     */
    @FXML
    protected void handleAddDepartment() {
        openDepartmentWindow(null);
    }

    /**
     * Charge et affiche la boîte de dialogue modale du formulaire de département.
     * <p>
     * Cette méthode instancie le fichier FXML du formulaire, extrait son sous-contrôleur
     * {@link DepartmentFormController} pour y injecter le département sélectionné (ou {@code null}
     * en mode création), et bloque la fenêtre parente jusqu'à la fermeture du sous-panneau.
     * Un rafraîchissement automatique de la table est opéré à sa fermeture.
     * </p>
     *
     * @param department L'instance de {@link Department} à éditer, ou {@code null} s'il s'agit d'une création.
     */
    private void openDepartmentWindow(Department department) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/department/department-form.fxml"));
            Parent root = loader.load();

            DepartmentFormController controller = loader.getController();
            controller.setDepartment(department);

            Stage stage = new Stage();
            stage.setTitle(department == null ? "Nouveau Département" : "Éditer le Département");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshTable();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }
}