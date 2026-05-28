package com.example.mainapp.controller;

import com.example.mainapp.model.Department;
import com.example.mainapp.network.TCPServer;
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

public class DepartmentController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, String> colDeptId;
    @FXML private TableColumn<Department, String> colDeptNom;
    @FXML private TableColumn<Department, String> colDeptNbEmp;

    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        // 1. Lier la colonne ID
        colDeptId.setCellValueFactory(cellData -> {
            if (cellData.getValue().getId() != null) {
                return new SimpleStringProperty(cellData.getValue().getId().toString());
            }
            return new SimpleStringProperty("N/A");
        });

        // 2. Lier la colonne Nom
        colDeptNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        // 3. Lier la colonne Effectif
        colDeptNbEmp.setCellValueFactory(cellData -> {
            Department dept = cellData.getValue();
            int nbEmployes = (dept.getEmployees() != null) ? dept.getEmployees().size() : 0;
            return new SimpleStringProperty(nbEmployes + " employé(s)");
        });

        // 4. Écouteur de double-clic
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
            departmentTable.refresh();
        }
    }

    @FXML
    protected void handleAddDepartment() {
        ouvrirFenetreDepartement(null); // Null indique le mode création
    }

    private void ouvrirFenetreDepartement(Department departement) {
        try {
            // 1. Charger le fichier FXML du formulaire
            // ⚠️ Ajuste le chemin si ton fichier ne se trouve pas directement à cet endroit
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/department-form.fxml"));
            Parent root = loader.load();

            // 2. Récupérer le contrôleur du formulaire et lui passer le département
            DepartmentFormController controller = loader.getController();
            controller.setDepartment(departement);

            // 3. Configurer la nouvelle fenêtre (Stage)
            Stage stage = new Stage();
            stage.setTitle(departement == null ? "Nouveau Département" : "Éditer le Département");
            stage.setScene(new Scene(root));

            // Rend la fenêtre modale (bloque les clics sur la fenêtre principale en arrière-plan)
            stage.initModality(Modality.APPLICATION_MODAL);

            // 4. Afficher la fenêtre et mettre le code en pause jusqu'à sa fermeture
            stage.showAndWait();

            // 5. Rafraîchir le tableau automatiquement dès que l'utilisateur ferme le formulaire
            rafraichirTableau();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du formulaire FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }
}