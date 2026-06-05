package com.example.mainapp.controller.departement;

import com.example.mainapp.model.department.Department;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Contrôleur graphique gérant le formulaire modal de création et d'édition d'un département.
 * <p>
 * Cette classe prend en charge l'affichage contextuel de la boîte de dialogue : elle bascule
 * entre le mode création (champs vides, bouton de suppression masqué) et le mode édition
 * (champs pré-remplis avec les données de l'entité, bouton de suppression visible).
 * Elle intercepte les actions utilisateur pour valider et transmettre les modifications
 * à la couche métier {@link DepartmentService}.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class DepartmentFormController {

    @FXML /** Titre dynamique de la fenêtre modale (ex: "Nouveau Département" ou "Éditer Département"). */
    private Label titleLabel;

    @FXML /** Champ de saisie textuel pour le nom du département. */
    private TextField nomField;

    @FXML /** Bouton de suppression, affiché exclusivement en mode édition. */
    private Button btnDelete;

    /** L'instance du département actuellement manipulée au sein du formulaire. */
    private Department currentDepartment;

    /** Drapeau logique indiquant si le formulaire est ouvert en mode création (vrai) ou édition (faux). */
    private boolean isCreationMode;

    /**
     * Injecte le département à traiter et configure l'état visuel initial du formulaire.
     * <p>
     * Si l'objet fourni est {@code null}, le contrôleur s'initialise en mode création en instanciant
     * un département vierge et en masquant le bouton de suppression. Dans le cas contraire, il extrait
     * le nom du département existant pour l'afficher dans le champ de saisie.
     * </p>
     *
     * @param department L'instance de {@link Department} à modifier, ou {@code null} s'il s'agit d'un ajout.
     */
    public void setDepartment(Department department) {
        if (department == null) {
            this.isCreationMode = true;
            this.currentDepartment = new Department("");
            titleLabel.setText("Nouveau Département");
            btnDelete.setVisible(false);
        } else {
            this.isCreationMode = false;
            this.currentDepartment = department;
            titleLabel.setText("Éditer Département");
            btnDelete.setVisible(true);
            nomField.setText(department.getName());
        }
    }

    /**
     * Action FXML qui enregistre ou met à jour les données du département.
     * <p>
     * Extrait la saisie textuelle du champ de nom, nettoie les espaces superflus (trim),
     * applique la modification sur l'objet métier, puis invoque le service d'arrière-plan approprié.
     * Si une contrainte métier est violée (ex: doublon de nom), l'exception levée par le service
     * est interceptée pour afficher une boîte de dialogue d'alerte graphique à l'administrateur.
     * </p>
     */
    @FXML
    private void handleSave() {
        String newName = nomField.getText().trim();
        currentDepartment.setName(newName);

        try {
            if (isCreationMode) {
                DepartmentService.getInstance().createDepartment(currentDepartment);
            } else {
                DepartmentService.getInstance().updateDepartment(currentDepartment);
            }
            closeWindow();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'enregistrer");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Action FXML déclenchant le processus de suppression du département affiché.
     * <p>
     * Déploie une boîte de dialogue de confirmation (Confirmation Alert). Si l'utilisateur clique
     * sur le bouton de validation, la méthode sollicite le {@link DepartmentService}. Toute erreur
     * d'intégrité référentielle (ex: présence d'employés encore rattachés au service) bloque le processus
     * et génère une alerte graphique d'erreur.
     * </p>
     */
    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le département ?");
        confirm.setContentText("Voulez-vous vraiment supprimer " + currentDepartment.getName() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DepartmentService.getInstance().deleteDepartment(currentDepartment);
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
     * Abandonne les modifications volatiles en cours et ordonne la fermeture de la fenêtre modale.
     * </p>
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * Ferme la fenêtre graphique modale active.
     * <p>
     * Extrait de manière dynamique l'instance du conteneur de scène (Stage) depuis le composant
     * {@code nomField} pour déclencher sa fermeture propre.
     * </p>
     */
    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}