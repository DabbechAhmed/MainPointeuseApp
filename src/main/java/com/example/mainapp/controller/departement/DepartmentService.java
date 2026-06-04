package com.example.mainapp.controller.departement;

import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.department.Department;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;

import java.util.List;

public class DepartmentService {

    private static DepartmentService instance;
    private final Company company;

    private DepartmentService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    public static DepartmentService getInstance() {
        if (instance == null) {
            instance = new DepartmentService();
        }
        return instance;
    }

    public void creerDepartement(Department nouveauDept) throws Exception {
        if (nouveauDept.getName() == null || nouveauDept.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département est obligatoire.");
        }

        boolean nomExisteDeja = company.getDepartments().stream()
                .anyMatch(d -> d.getName().equalsIgnoreCase(nouveauDept.getName().trim()));

        if (nomExisteDeja) {
            throw new Exception("Un département portant ce nom existe déjà.");
        }

        company.addDepartment(nouveauDept);
        sauvegarderDonnees();
    }

    public List<Department> recupererTousLesDepartements() {
        return company.getDepartments();
    }

    public void mettreAJourDepartement(Department deptModifie) throws Exception {
        if (deptModifie == null || deptModifie.getName().trim().isEmpty()) {
            throw new Exception("Le nom du département ne peut pas être vide.");
        }
        sauvegarderDonnees();
    }

    public void supprimerDepartement(Department deptASupprimer) throws Exception {
        if (deptASupprimer == null) {
            throw new Exception("Impossible de supprimer un département nul.");
        }

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

    private void sauvegarderDonnees() {
        try {
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données de l'entreprise (Départements) mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur critique lors de la sauvegarde : " + e.getMessage());
        }
    }
}