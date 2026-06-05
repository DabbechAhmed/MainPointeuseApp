package com.example.mainapp.controller.departement;

import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.department.Department;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;

import java.util.List;

/**
 * Service métier centralisé gérant le cycle de vie et les opérations des départements.
 * <p>
 * Cette classe implémente le pattern Singleton pour centraliser la logique métier et les règles
 * d'intégrité référentielle liées aux services/départements de l'entreprise. Elle agit comme une
 * façade isolant le modèle {@link Company}. Elle applique des validations défensives cruciales,
 * telles que la vérification de l'unicité des noms (insensible à la casse) et le blocage strict de
 * la suppression d'un département si celui-ci contient encore des employés actifs.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class DepartmentService {

    /** L'instance unique (Singleton) du service des départements. */
    private static DepartmentService instance;

    /** La référence vers l'entité globale de l'entreprise pour manipuler le modèle de données. */
    private final Company company;

    /**
     * Constructeur privé extrayant l'instance de l'entreprise depuis le serveur TCP actif.
     */
    private DepartmentService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    /**
     * Retourne l'instance unique et globale de ce service.
     * <p>
     * Initialise l'instance de manière paresseuse (Lazy Initialization) lors de son tout premier appel.
     * </p>
     *
     * @return L'instance unique de {@link DepartmentService}.
     */
    public static DepartmentService getInstance() {
        if (instance == null) {
            instance = new DepartmentService();
        }
        return instance;
    }

    /**
     * Valide et enregistre un nouveau département au sein de l'organisation.
     * <p>
     * Cette méthode applique deux règles de gestion strictes :
     * <ul>
     * <li>Le nom du département ne doit pas être vide ou constitué uniquement d'espaces.</li>
     * <li>Le nom doit être unique au sein de l'entreprise (vérification insensible à la casse via un Stream).</li>
     * </ul>
     * Si les règles sont respectées, le département est ajouté et les données sont persistées sur le disque.
     * </p>
     *
     * @param newDepartment L'instance du département à créer et enregistrer.
     * @throws Exception Si le nom est manquant ou si un département homologue possède déjà ce nom.
     */
    public void createDepartment(Department newDepartment) throws Exception {
        if (newDepartment.getName() == null || newDepartment.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département est obligatoire.");
        }

        boolean nomExisteDeja = company.getDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase(newDepartment.getName().trim()));

        if (nomExisteDeja) {
            throw new Exception("Un département portant ce nom existe déjà.");
        }

        company.addDepartment(newDepartment);
        saveData();
    }

    /**
     * Récupère la liste complète de tous les départements enregistrés dans le système.
     *
     * @return Une {@link List} contenant l'ensemble des objets {@link Department}.
     */
    public List<Department> getAllDepartments() {
        return company.getDepartments();
    }

    /**
     * Valide et enregistre les modifications apportées à un département existant.
     * <p>
     * Vérifie la conformité du nouveau nom avant de déclencher la mise à jour sur le support de persistance.
     * </p>
     *
     * @param updatedDepartment L'objet département contenant les valeurs modifiées.
     * @throws Exception Si la référence est nulle ou si le nom modifié est vide.
     */
    public void updateDepartment(Department updatedDepartment) throws Exception {
        if (updatedDepartment == null || updatedDepartment.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département ne peut pas être vide.");
        }
        saveData();
    }

    /**
     * Supprime un département du registre de l'entreprise après contrôle d'intégrité.
     * <p>
     * <b>Règle d'intégrité référentielle :</b> L'opération est immédiatement avortée et lève une exception
     * si le département contient encore au moins un employé rattaché, garantissant ainsi qu'aucun employé
     * ne se retrouve sans affectation valide.
     * </p>
     *
     * @param departmentToDelete L'objet {@link Department} à retirer du système.
     * @throws Exception Si le département est nul, introuvable, ou s'il possède encore des employés affectés.
     */
    public void deleteDepartment(Department departmentToDelete) throws Exception {
        if (departmentToDelete == null) {
            throw new Exception("Impossible de supprimer un département nul.");
        }

        if (departmentToDelete.getEmployees() != null && !departmentToDelete.getEmployees().isEmpty()) {
            throw new Exception("Impossible de supprimer ce département : il contient encore "
                    + departmentToDelete.getEmployees().size() + " employé(s). "
                    + "Veuillez d'abord réaffecter ces employés.");
        }

        boolean supprime = company.getDepartments().remove(departmentToDelete);
        if (!supprime) {
            throw new Exception("Le département n'a pas été trouvé dans le système.");
        }

        saveData();
    }

    /**
     * Déclenche la sérialisation et l'écriture de l'état de l'entreprise sur le disque local.
     * <p>
     * Intercepte en interne toute exception liée à la persistance afin de consigner un log d'erreur
     * sans interrompre l'expérience utilisateur sur l'interface graphique.
     * </p>
     */
    private void saveData() {
        try {
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données de l'entreprise (Départements) mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur critique lors de la sauvegarde : " + e.getMessage());
        }
    }
}