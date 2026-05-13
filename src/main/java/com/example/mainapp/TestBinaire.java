package com.example.mainapp;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Department;
import com.example.mainapp.model.Employee;
import com.example.mainapp.enums.Status; // N'oublie pas l'import de l'Enum
import com.example.mainapp.service.PersistenceManager;

public class TestBinaire {

    public static void main(String[] args) {

        System.out.println("=== 1. PRÉPARATION DES DONNÉES ===");

        // Création de l'entreprise racine
        Company maCompagnie = new Company("Polytech Tours");

        // 1. On crée d'abord le département
        Department deptIT = new Department("Informatique");

        // 2. On crée l'employé en lui donnant l'objet département créé juste au-dessus
        Employee emp1 = new Employee(deptIT, "Dupont", "Jean", Status.EMP);

        // 3. On établit les liens dans les listes
        maCompagnie.addDepartment(deptIT);
        maCompagnie.addEmployee(emp1);

        // Optionnel mais propre : ajouter l'employé à la liste interne du département
        deptIT.addEmployee(emp1);

        System.out.println("Données créées : " + emp1.getName() + " travaille au service " + emp1.getDepartment().getName());

        System.out.println("\n=== 2. TEST DE SAUVEGARDE ===");
        PersistenceManager.saveData(maCompagnie);

        System.out.println("\n=== 3. TEST DE CHARGEMENT ===");
        Company compagnieChargee = PersistenceManager.loadData();

        System.out.println("\n=== 4. VÉRIFICATION DE L'INTÉGRITÉ ===");
        if (compagnieChargee != null && !compagnieChargee.getEmployees().isEmpty()) {
            Employee recup = compagnieChargee.getEmployees().get(0);

            System.out.println("✅ Entreprise : " + compagnieChargee.getName());
            System.out.println("✅ Employé récupéré : " + recup.getName() + " " + recup.getSurname());

            // C'est ici qu'on voit si l'objet Department a bien été sauvegardé avec
            if (recup.getDepartment() != null) {
                System.out.println("✅ Département récupéré : " + recup.getDepartment().getName());
            } else {
                System.out.println("❌ Erreur : Le département est null !");
            }
        }
    }
}