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

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colEmpId;
    @FXML private TableColumn<Employee, String> colEmpNom;
    @FXML private TableColumn<Employee, String> colEmpPrenom;

    @FXML private TableColumn<Employee, String> colEmpDept;
    @FXML private TableColumn<Employee, String> colEmpStatus;
    @FXML private TableColumn<Employee, String> colEmpSolde;

    @FXML private TextField searchField;
    private ObservableList<Employee> allEmployees;

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

        searchField.textProperty().addListener((observable,oldValue,newValue )->filtrerEmployes(newValue) );
    }



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
            rafraichirTableau();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture du formulaire employé.");
        }
    }
    public void rafraichirTableau() {
        List<Employee> listEmployee = EmployeeService.getInstance().recupererTousLesEmployes();

        if (listEmployee == null) {
            listEmployee = new ArrayList<>();
        }

        allEmployees = FXCollections.observableArrayList(listEmployee);
        employeeTable.setItems(allEmployees);
        employeeTable.refresh();

        searchField.clear();
    }

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


    @FXML
    protected void handleAddEmployee() {
        ouvrirFenetreEmploye(null);
    }
}