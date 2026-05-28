package com.example.mainapp.controller;

import com.example.mainapp.model.Department;
import com.example.mainapp.model.Employee;
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

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpNom;
    @FXML private TableColumn<Employee, String> colEmpPrenom;

    // ✅ 1. Déclaration des nouvelles colonnes
    @FXML private TableColumn<Employee, String> colEmpDept;
    @FXML private TableColumn<Employee, String> colEmpStatus;
    @FXML private TableColumn<Employee, String> colEmpSolde;

    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        // Lier les colonnes existantes
        colEmpId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        colEmpNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colEmpPrenom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSurname()));

        // ✅ 2. Lier la colonne Département
        colEmpDept.setCellValueFactory(cellData -> {
            Department dept = cellData.getValue().getDepartment();
            // On vérifie si l'employé a un département pour éviter une erreur NullPointerException
            return new SimpleStringProperty(dept != null ? dept.getName() : "Non assigné");
        });


        // ✅ 3. Lier la colonne Status
        colEmpStatus.setCellValueFactory(cellData -> {
            // getStatus() renvoie l'Enum Status. On utilise .name() pour récupérer le texte (ex: "EMP", "ADMIN")
            return new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : "N/A");
        });

        // ✅ 4. Lier la colonne Solde Minutes
        colEmpSolde.setCellValueFactory(cellData -> {
            // getSoldeMinutes() renvoie un long (nombre). On le concatène avec " min" pour faire une belle chaîne de caractères
            long solde = cellData.getValue().getSoldeMinutes();
            return new SimpleStringProperty(solde + " min");
        });
        employeeTable.setRowFactory(tv -> {
            TableRow<Employee> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Si on double-clique et que la ligne n'est pas vide
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Employee rowData = row.getItem();
                    ouvrirFenetreEmploye(rowData); // Ouvre en mode Édition
                }
            });
            return row;
        });
    }



    // ✅ NOUVEAU : La méthode magique qui crée la fenêtre modale (Pop-up)
    private void ouvrirFenetreEmploye(Employee employe) {
        try {
            // 1. Charger le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/mainapp/view/employee-form.fxml"));
            // (Assure-toi que le chemin vers employee-form.fxml correspond à la structure de ton projet)
            Parent root = loader.load();

            // 2. Récupérer le contrôleur pour lui injecter l'employé
            EmployeeFormController controller = loader.getController();
            controller.setEmployee(employe);

            // 3. Créer la nouvelle fenêtre par-dessus l'ancienne
            Stage stage = new Stage();
            stage.setTitle(employe == null ? "Ajouter Employé" : "Éditer Employé");
            stage.setScene(new Scene(root));

            // 4. Bloquer l'application principale tant que ce pop-up est ouvert
            stage.initModality(Modality.APPLICATION_MODAL);

            // 5. Afficher et attendre que l'utilisateur ferme la fenêtre
            stage.showAndWait();

            // 6. Une fois la fenêtre fermée, on met à jour le tableau pour voir les changements
            rafraichirTableau();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du formulaire employé.");
        }
    }

    public void rafraichirTableau() {
        var company = TCPServer.getInstance().getCompany();
        if (company != null) {
            ObservableList<Employee> empList = FXCollections.observableArrayList(company.getEmployees());
            employeeTable.setItems(empList);
        }
    }

    @FXML
    protected void handleAddEmployee() {
        ouvrirFenetreEmploye(null);
    }
}