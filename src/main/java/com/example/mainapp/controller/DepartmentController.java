package com.example.mainapp.controller;

import com.example.mainapp.model.Department;
import com.example.mainapp.network.TCPServer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DepartmentController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> colDeptId;
    @FXML private TableColumn<Department, String> colDeptNom;
    @FXML private TableColumn<Department, String> colDeptNbEmp;

    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        // 1. Lier la colonne ID (avec sécurité au cas où l'ID est null)
        colDeptId.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId() != null) {
                return new SimpleStringProperty(cellData.getValue().getId().toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // 2. Lier la colonne Nom
        colDeptNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // 3. Lier la colonne Effectif (Calcul dynamique de la taille de la liste)
        colDeptNbEmp.setCellValueFactory(cellData -> {
            Department dept = cellData.getValue();
            int nbEmployes = (dept.getEmployees() != null) ? dept.getEmployees().size() : 0;
            return new SimpleStringProperty(nbEmployes + " employé(s)");
        });

        // 4. Ajouter l'écouteur de double-clic pour l'édition future
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
        var company = TCPServer.getInstance().getCompany();

        if (company != null && company.getDepartments() != null) {
            ObservableList<Department> deptList = FXCollections.observableArrayList(company.getDepartments());
            departmentTable.setItems(deptList);
        }
    }

    @FXML
    protected void handleAddDepartment() {
        ouvrirFenetreDepartement(null); // Mode création
    }

    private void ouvrirFenetreDepartement(Department departement) {
        // Ce bloc sera remplacé par le FXMLLoader de la fenêtre modale
        if (departement == null) {
            System.out.println("Bientôt : Fenêtre pour CRÉER un département.");
        } else {
            System.out.println("Bientôt : Fenêtre pour MODIFIER le département : " + departement.getName());
        }
    }
}