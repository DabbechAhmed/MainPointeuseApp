package com.example.mainapp.controller.departement;

import com.example.mainapp.model.department.Department;
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
import java.util.List;

public class DepartmentController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> colDeptId;
    @FXML private TableColumn<Department, String> colDeptNom;
    @FXML private TableColumn<Department, String> colDeptNbEmp;

    @FXML private TextField searchField;

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
            int nbEmployes = (dept.getEmployees() != null) ? dept.getEmployees().size() : 0;
            return new SimpleStringProperty(nbEmployes + " employé(s)");
        });

        departmentTable.setRowFactory(tv -> {
            TableRow<Department> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Department rowData = row.getItem();
                    ouvrirFenetreDepartement(rowData);
                }
            });
            return row;
        });
    }

    public void rafraichirTableau() {
        List<Department> listeDepartement = DepartmentService.getInstance().recupererTousLesDepartements();

        if (listeDepartement == null) {
            listeDepartement = new java.util.ArrayList<>();
        }

        ObservableList<Department> deptList = FXCollections.observableArrayList(listeDepartement);
        departmentTable.setItems(deptList);
        departmentTable.refresh();
    }

    @FXML
    protected void handleAddDepartment() {
        ouvrirFenetreDepartement(null);
    }

    private void ouvrirFenetreDepartement(Department departement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/department/department-form.fxml"));
            Parent root = loader.load();

            DepartmentFormController controller = loader.getController();
            controller.setDepartment(departement);

            Stage stage = new Stage();
            stage.setTitle(departement == null ? "Nouveau Département" : "Éditer le Département");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            rafraichirTableau();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }
}