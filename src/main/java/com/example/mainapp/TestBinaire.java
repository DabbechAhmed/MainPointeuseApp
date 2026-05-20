package com.example.mainapp;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Department;
import com.example.mainapp.model.Employee;
import com.example.mainapp.enums.Status;
import com.example.mainapp.service.PersistenceManager;

public class TestBinaire {

    public static void main(String[] args) {

        System.out.println("=== 1. PRÉPARATION DES DONNÉES ===");

        // Création de l'entreprise racine
        Company maCompagnie = new Company("Polytech Tours");

        // 1. On crée le département
        Department deptIT = new Department("Informatique");

        // 2. On crée les employés (SANS Dupont)
        Employee emp1 = new Employee(deptIT, "M'SADAA", "Youssef", Status.EMP);
        Employee emp2 = new Employee(deptIT, "DEBBACH", "Ahmed", Status.EMP);
        Employee emp3 = new Employee(deptIT, "RIANI", "Youssef", Status.EMP);
        Employee emp4 = new Employee(deptIT, "BEN ABDA", "Mohamed Yassine", Status.EMP);
        Employee emp5 = new Employee(deptIT, "EL YAHYAOUI", "Youssef", Status.EMP);

        // 3. On établit les liens
        maCompagnie.addDepartment(deptIT);
        maCompagnie.addEmployee(emp1);
        maCompagnie.addEmployee(emp2);
        maCompagnie.addEmployee(emp3);
        maCompagnie.addEmployee(emp4);
        maCompagnie.addEmployee(emp5);

        // On les ajoute aussi au département
        deptIT.addEmployee(emp1);
        deptIT.addEmployee(emp2);
        deptIT.addEmployee(emp3);
        deptIT.addEmployee(emp4);
        deptIT.addEmployee(emp5);

        System.out.println("✅ Données générées avec succès (5 employés).");

        System.out.println("\n=== 2. TEST DE SAUVEGARDE ===");
        PersistenceManager.saveData(maCompagnie);

        System.out.println("\n=== 3. TEST DE CHARGEMENT ===");
        Company compagnieChargee = PersistenceManager.loadData();

        System.out.println("\n=== 4. VÉRIFICATION DE L'INTÉGRITÉ ===");
        if (compagnieChargee != null && !compagnieChargee.getEmployees().isEmpty()) {

            System.out.println("🏢 Entreprise : " + compagnieChargee.getName());
            System.out.println("👥 Nombre d'employés récupérés : " + compagnieChargee.getEmployees().size());
            System.out.println("-------------------------------------------------");

            // ✅ On utilise une boucle pour afficher TOUT LE MONDE
            for (Employee recup : compagnieChargee.getEmployees()) {
                String nomDept = (recup.getDepartment() != null) ? recup.getDepartment().getName() : "Aucun";
                System.out.println("👤 " + recup.getName() + " " + recup.getSurname() + " | Dpt: " + nomDept);
            }
            System.out.println("-------------------------------------------------");
        }
    }
}