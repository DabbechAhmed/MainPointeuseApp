package com.example.mainapp.model.department;

import com.example.mainapp.model.employee.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Représente un département au sein d'une entreprise.
 * Contient un identifiant unique, un nom et la liste des employés affectés.
 */
public class Department implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Identifiant unique du département */
    private UUID id;
    /** Nom du département */
    private String name;
    /** Employés rattachés au département */
    private List<Employee> employees;

    /**
     * Crée un département avec un nom et génère automatiquement un identifiant.
     * @param name nom du département
     */
    public Department(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.employees = new ArrayList<>();
    }

    /**
     * Retourne l'identifiant du département.
     * @return id du département
     */
    public UUID getId() { return id; }

    /**
     * Définit l'identifiant du département.
     * @param id nouvel identifiant
     */
    public void setId(UUID id) { this.id = id; }

    /**
     * Retourne le nom du département.
     * @return nom
     */
    public String getName() { return name; }

    /**
     * Définit le nom du département.
     * @param name nouveau nom
     */
    public void setName(String name) { this.name = name; }

    /**
     * Retourne la liste des employés du département.
     * @return liste des employés
     */
    public List<Employee> getEmployees() { return employees; }

    /**
     * Ajoute un employé s'il n'est pas nul et n'est pas déjà présent.
     * @param employee employé à ajouter
     */
    public void addEmployee(Employee employee) {
        if (employee != null && !this.employees.contains(employee)) {
            this.employees.add(employee);
        }
    }

    /**
     * Supprime un employé du département.
     * @param employee employé à supprimer
     */
    public void removeEmployee(Employee employee) {
        this.employees.remove(employee);
    }

    /**
     * Retourne le nom du département (utile pour l'affichage).
     * @return nom
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * L'égalité est basée sur l'identifiant unique.
     * @param o autre objet
     * @return true si même id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Hash code basé sur l'identifiant.
     * @return code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}