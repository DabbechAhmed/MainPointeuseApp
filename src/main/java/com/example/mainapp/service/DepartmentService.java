package com.example.mainapp.service;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Department;
import com.example.mainapp.network.TCPServer;

import java.util.List;

public class DepartmentService {

    // 1. Instance unique (Singleton)
    private static DepartmentService instance;

    private final Company company;

    // 2. Constructeur privé
    private DepartmentService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    // 3. Récupération de l'instance
    public static DepartmentService getInstance() {
        if (instance == null) {
            instance = new DepartmentService();
        }
        return instance;
    }

    // ==========================================
    // 🟩 CREATE (Ajouter)
    // ==========================================
    public void creerDepartement(Department nouveauDept) throws Exception {
        // Validation : Le nom ne doit pas être vide
        if (nouveauDept.getName() == null || nouveauDept.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département est obligatoire.");
        }

        // Validation : Vérifier si un département avec ce nom exact existe déjà
        boolean nomExisteDeja = company.getDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase(nouveauDept.getName().trim()));

        if (nomExisteDeja) {
            throw new Exception("Un département portant ce nom existe déjà.");
        }

        company.addDepartment(nouveauDept); // Ou company.getDepartments().add(nouveauDept) selon ta classe Company
        sauvegarderDonnees();
    }

    // ==========================================
    // 🟦 READ (Lire)
    // ==========================================
    public List<Department> recupererTousLesDepartements() {
        return company.getDepartments();
    }

    // ==========================================
    // 🟧 UPDATE (Mettre à jour)
    // ==========================================
    public void mettreAJourDepartement(Department deptModifie) throws Exception {
        if (deptModifie == null || deptModifie.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département ne peut pas être vide.");
        }
        // L'objet est déjà modifié en mémoire via l'interface, on déclenche juste la sauvegarde
        sauvegarderDonnees();
    }

    // ==========================================
    // 🟥 DELETE (Supprimer)
    // ==========================================
    public void supprimerDepartement(Department deptASupprimer) throws Exception {
        if (deptASupprimer == null) {
            throw new Exception("Impossible de supprimer un département nul.");
        }

        // 🛡️ RÈGLE MÉTIER TRÈS IMPORTANTE : Ne pas supprimer si non vide !
        if (deptASupprimer.getEmployees() != null && !deptASupprimer.getEmployees().isEmpty()) {
            throw new Exception("Impossible de supprimer ce département : il contient encore "
                    + deptASupprimer.getEmployees().size() + " employé(s). "
                    + "Veuillez d'abord réaffecter ces employés.");
        }

        boolean supprime = company.getDepartments().remove(deptASupprimer);
        if (!supprime) {
            throw new Exception("Le département n'a pas été trouvé dans le système.");
        }

        sauvegarderDonnees();
    }

    // ==========================================
    // 💾 PERSISTANCE (Sauvegarde)
    // ==========================================
    private void sauvegarderDonnees() {
        try {
            // Fait appel à ta classe de persistance pour sauvegarder la compagnie entière
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données de l'entreprise (Départements) mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur critique lors de la sauvegarde : " + e.getMessage());
        }
    }
}