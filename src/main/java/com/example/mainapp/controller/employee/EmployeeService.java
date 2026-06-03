package com.example.mainapp.controller.employee;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.PersistenceManager;

import java.util.List;
import java.util.UUID;

public class EmployeeService {

    private static EmployeeService instance;

    private final Company company;

    private EmployeeService() {
        this.company = TCPServer.getInstance().getCompany();
        if (this.company == null) {
            throw new IllegalStateException("Company non initialisée. Appelle TCPServer.demarrer(...) avant d'utiliser EmployeeService.");
        }
    }

    public static EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    public void creerEmploye(Employee nouvelEmploye) throws Exception {
        if (nouvelEmploye == null) {
            throw new Exception("Employé invalide (null).");
        }
        if (nouvelEmploye.getName() == null || nouvelEmploye.getName().trim().isEmpty()) {
            throw new Exception("Le nom de l'employé est obligatoire.");
        }
        if (nouvelEmploye.getSurname() == null || nouvelEmploye.getSurname().trim().isEmpty()) {
            throw new Exception("Le prénom de l'employé est obligatoire.");
        }

        company.addEmployee(nouvelEmploye);

        sauvegarderDonnees();
    }

    public List<Employee> recupererTousLesEmployes() {
        return company.getEmployees();
    }

    public Employee recupererEmployeParId(UUID id) throws Exception {
        if (id == null) throw new Exception("Id invalide.");
        Employee emp = company.findEmployeeById(id);
        if (emp == null) throw new Exception("Employé introuvable : " + id);
        return emp;
    }

    public void mettreAJourEmploye(Employee employeModifie) throws Exception {
        if (employeModifie == null) {
            throw new Exception("L'employé à modifier est invalide.");
        }
        if (employeModifie.getName() == null || employeModifie.getName().trim().isEmpty()) {
            throw new Exception("Le nom de l'employé est obligatoire.");
        }
        if (employeModifie.getSurname() == null || employeModifie.getSurname().trim().isEmpty()) {
            throw new Exception("Le prénom de l'employé est obligatoire.");
        }

        sauvegarderDonnees();
    }

    public void supprimerEmploye(Employee employeASupprimer) throws Exception {
        if (employeASupprimer == null) {
            throw new Exception("Impossible de supprimer un employé nul.");
        }
        company.removeEmployee(employeASupprimer.getId());
        sauvegarderDonnees();
    }

    private void sauvegarderDonnees() {
        PersistenceManager.saveData(company);
    }
}
