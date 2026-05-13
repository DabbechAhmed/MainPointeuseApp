package com.example.mainapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Company implements Serializable {

    // Le passeport de sérialisation dont on a parlé !
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    // On utilise des interfaces (List) pour le type,
    // mais on instanciera des ArrayList dans le constructeur
    private List<Employee> employees;
    private List<Department> departments;

    /**
     * Constructeur par défaut.
     * Très important d'initialiser les listes ici pour éviter
     * les fameuses erreurs "NullPointerException" plus tard.
     */
    public Company(String name) {
        this.name = name;
        this.employees = new ArrayList<>();
        this.departments = new ArrayList<>();
    }

    // ==========================================
    // MÉTHODES DE GESTION DES EMPLOYÉS
    // ==========================================

    public void addEmployee(Employee employee) {
        if (employee != null && !this.employees.contains(employee)) {
            this.employees.add(employee);
        }
    }

    public void removeEmployee(UUID id) {
        // La méthode removeIf est très pratique en Java moderne
        // Elle parcourt la liste et supprime l'élément si l'ID correspond
        this.employees.removeIf(emp -> emp.getId().equals(id));
    }

    public Employee findEmployeeById(UUID id) {
        for (Employee emp : employees) {
            if (emp.getId().equals(id)) {
                return emp;
            }
        }
        return null; // Retourne null si l'employé n'existe pas
    }

    // ==========================================
    // MÉTHODES DE GESTION DES DÉPARTEMENTS
    // ==========================================

    public void addDepartment(Department dept) {
        if (dept != null && !this.departments.contains(dept)) {
            this.departments.add(dept);
        }
    }

    public void removeDepartment(String deptName) {
        this.departments.removeIf(dept -> dept.getName().equalsIgnoreCase(deptName));
    }

    // ==========================================
    // GETTERS & SETTERS CLASSIQUES
    // ==========================================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Department> getDepartments() {
        return departments;
    }
}