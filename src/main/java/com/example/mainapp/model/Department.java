package com.example.mainapp.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID; // ✅ NOUVEL IMPORT

public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ✅ NOUVEL ATTRIBUT
    private UUID id;
    private String name;
    private List<Employee> employees;

    /**
     * Constructeur
     */
    public Department(String name) {
        this.id = UUID.randomUUID(); // ✅ Génération automatique de l'ID à la création
        this.name = name;
        this.employees = new ArrayList<>();
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    // ✅ NOUVEAU GETTER/SETTER
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Employee> getEmployees() { return employees; }

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
    // MÉTHODES REDÉFINIES
    // ==========================================

    @Override
    public String toString() {
        return this.name;
    }

    // ✅ TRÈS IMPORTANT : L'égalité se fait maintenant sur l'ID, plus sur le nom !
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}