package com.example.mainapp.controller.departement;

import com.example.mainapp.model.department.Department;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class DepartmentFormController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private Button btnDelete;

    private Department departmentActuel;
    private boolean isCreationMode;

    public void setDepartment(Department dept) {
        if (dept == null) {
            this.isCreationMode = true;
            this.departmentActuel = new Department("");
            titleLabel.setText("Nouveau Département");
            btnDelete.setVisible(false);
        } else {
            this.isCreationMode = false;
            this.departmentActuel = dept;
            titleLabel.setText("Éditer Département");
            btnDelete.setVisible(true);
            nomField.setText(dept.getName());
        }
    }

    @FXML
    private void handleSave() {
        String nouveauNom = nomField.getText().trim();
        departmentActuel.setName(nouveauNom);

        try {
            if (isCreationMode) {
                DepartmentService.getInstance().creerDepartement(departmentActuel);
            } else {
                DepartmentService.getInstance().mettreAJourDepartement(departmentActuel);
            }
            fermerFenetre();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'enregistrer");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le département ?");
        confirm.setContentText("Voulez-vous vraiment supprimer " + departmentActuel.getName() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DepartmentService.getInstance().supprimerDepartement(departmentActuel);
                fermerFenetre();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Suppression impossible");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }

    }

    @FXML
    private void handleCancel() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}