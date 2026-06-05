package com.example.mainapp.controller.employee;

import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;

import java.util.List;
import java.util.UUID;

/**
 * Service métier centralisé gérant le cycle de vie et les opérations des employés.
 * <p>
 * Cette classe implémente le pattern Singleton de manière stricte pour centraliser la logique
 * d'analyse, de validation et d'intégrité des fiches employés. Elle sert de passerelle (façade)
 * entre les contrôleurs graphiques (IHM) et le modèle de données global {@link Company}.
 * Chaque opération de modification (création, mise à jour, suppression) exécute des validations
 * défensives rigoureuses et déclenche immédiatement la persistance automatique sur le disque.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class EmployeeService {

    /** L'instance unique (Singleton) du service des employés. */
    private static EmployeeService instance;

    /** La référence vers l'entité globale de l'entreprise pour manipuler le modèle de données. */
    private final Company company;

    /**
     * Constructeur privé extrayant l'instance de l'entreprise depuis le serveur TCP actif.
     * <p>
     * Ce constructeur sécurise l'initialisation du service en s'assurant que la structure
     * {@link Company} est bien instanciée en mémoire réseau avant toute manipulation d'employés.
     * </p>
     * * @throws IllegalStateException Si le serveur TCP n'a pas été démarré au préalable.
     */
    private EmployeeService() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) {
            throw new IllegalStateException("Company non initialisée. Appelle TCPServer.demarrer(...) avant d'utiliser EmployeeService.");
        }
    }

    /**
     * Retourne l'instance unique et globale de ce service.
     * <p>
     * Initialise l'instance de manière paresseuse (Lazy Initialization) lors de son tout premier appel.
     * </p>
     *
     * @return L'instance unique de {@link EmployeeService}.
     */
    public static EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    /**
     * Valide et enregistre un nouvel employé au sein du système.
     * <p>
     * Cette méthode applique des règles de validation strictes : l'objet ne doit pas être nul,
     * et les champs textuels du nom et du prénom ne doivent pas être vides ou composés d'espaces.
     * Si l'objet passe ces validations, il est inséré dans l'entreprise et sauvegardé sur disque.
     * </p>
     *
     * @param newEmployee L'instance de l'employé à ajouter.
     * @throws Exception Si l'objet est nul ou si l'un des champs obligatoires (nom/prénom) est vide.
     */
    public void createEmployee(Employee newEmployee) throws Exception {
        if (newEmployee == null) {
            throw new Exception("Employé invalide (null).");
        }
        if (newEmployee.getName() == null || newEmployee.getName().trim().isEmpty()) {
            throw new Exception("Le nom de l'employé est obligatoire.");
        }
        if (newEmployee.getSurname() == null || newEmployee.getSurname().trim().isEmpty()) {
            throw new Exception("Le prénom de l'employé est obligatoire.");
        }

        company.addEmployee(newEmployee);
        saveData();
    }

    /**
     * Récupère la liste brute et complète de tous les employés enregistrés dans l'entreprise.
     *
     * @return Une {@link List} contenant l'ensemble des objets {@link Employee}.
     */
    public List<Employee> getAllEmployees() {
        return company.getEmployees();
    }

    /**
     * Recherche un employé unique à partir de son identifiant système.
     *
     * @param id L'identifiant {@link UUID} de l'employé recherché.
     * @return L'objet {@link Employee} correspondant s'il est localisé.
     * @throws Exception Si l'identifiant fourni est nul ou si aucun employé ne possède cet UUID.
     */
    public Employee getEmployeeById(UUID id) throws Exception {
        if (id == null) throw new Exception("Id invalide.");
        Employee emp = company.findEmployeeById(id);
        if (emp == null) throw new Exception("Employé introuvable : " + id);
        return emp;
    }

    /**
     * Applique et persiste les modifications apportées à la fiche d'un employé existant.
     * <p>
     * Réévalue les contraintes de validation sur le nom et le prénom avant de valider la mise à jour
     * et d'écrire le nouvel état du modèle sur le disque local.
     * </p>
     *
     * @param updatedEmployee L'objet employé modifié contenant les nouvelles valeurs.
     * @throws Exception Si l'objet est nul ou si les critères d'identité obligatoires sont invalidés.
     */
    public void updateEmployee(Employee updatedEmployee) throws Exception {
        if (updatedEmployee == null) {
            throw new Exception("L'employé à modifier est invalide.");
        }
        if (updatedEmployee.getName() == null || updatedEmployee.getName().trim().isEmpty()) {
            throw new Exception("Le nom de l'employé est obligatoire.");
        }
        if (updatedEmployee.getSurname() == null || updatedEmployee.getSurname().trim().isEmpty()) {
            throw new Exception("Le prénom de l'employé est obligatoire.");
        }

        saveData();
    }

    /**
     * Supprime définitivement un employé du registre de l'entreprise.
     * <p>
     * Extrait l'UUID de l'entité à supprimer pour la retirer de la collection centrale du modèle
     * avant de mettre à jour le fichier de persistance.
     * </p>
     *
     * @param employeeToDelete L'objet {@link Employee} à radier du système.
     * @throws Exception Si la référence de l'employé fournie est nulle.
     */
    public void deleteEmployee(Employee employeeToDelete) throws Exception {
        if (employeeToDelete == null) {
            throw new Exception("Impossible de supprimer un employé nul.");
        }
        company.removeEmployee(employeeToDelete.getId());
        saveData();
    }

    /**
     * Déclenche la sérialisation et l'écriture de l'état de l'entreprise sur le disque.
     * <p>
     * Méthode interne utilitaire déléguant la sauvegarde physique au {@link PersistenceManager}.
     * </p>
     */
    private void saveData() {
        PersistenceManager.saveData(company);
    }
}