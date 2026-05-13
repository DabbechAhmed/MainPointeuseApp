package com.example.mainapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Employee> employees;

    /**
     * Constructeur
     */
    public Department(String name) {
        this.name = name;
        this.employees = new ArrayList<>(); // Toujours initialiser la liste !
    }

    // ==========================================
    // MÉTHODES DE GESTION DES EMPLOYÉS
    // ==========================================

    public void addEmployee(Employee employee) {
        if (employee != null && !this.employees.contains(employee)) {
            this.employees.add(employee);
        }
    }

    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
    }

    // ==========================================
    // GETTERS & SETTERS
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

    // ==========================================
    // MÉTHODES REDÉFINIES (TRÈS IMPORTANTES)
    // ==========================================

    /**
     * L'affichage par défaut de l'objet.
     * C'est crucial pour JavaFX : si tu mets un objet Department dans une
     * ComboBox (liste déroulante), c'est cette méthode qui déterminera le texte affiché.
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Permet de comparer deux départements.
     * On considère que deux départements sont identiques s'ils ont le même nom.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}