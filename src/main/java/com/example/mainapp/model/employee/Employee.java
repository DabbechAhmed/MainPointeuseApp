package com.example.mainapp.model.employee;

import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.schedule.Schedule;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Représente un employé avec ses informations personnelles, son statut,
 * son service, son planning et son solde de minutes.
 */
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String surname;
    private Status status;
    private Department department;
    private Schedule schedule;
    private long soldeMinutes;

    /**
     * Constructeur par défaut. Initialise les champs avec des valeurs par défaut.
     */
    public Employee() {
        this.id = UUID.randomUUID();
        this.name = "";
        this.surname = "";
        this.status = Status.EMP;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    /**
     * Constructeur avec informations principales.
     * @param department département d'affectation
     * @param name prénom
     * @param surname nom de famille
     * @param status statut de l'employé
     */
    public Employee(Department department, String name, String surname, Status status) {
        this.id = UUID.randomUUID();
        this.department = department;
        this.name = name;
        this.surname = surname;
        this.status = status;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    /**
     * Modifie le solde de minutes en ajoutant la valeur fournie.
     * @param minutes nombre de minutes à ajouter (peut être négatif)
     */
    public void modifierSoldeMinutes(long minutes) {
        this.soldeMinutes += minutes;
    }


    /** Retourne l'identifiant de l'employé. */
    public UUID getId() { return id; }
    /** Définit l'identifiant de l'employé. */
    public void setId(UUID id) { this.id = id; }

    /** Retourne le département d'affectation. */
    public Department getDepartment() { return department; }
    /** Définit le département d'affectation. */
    public void setDepartment(Department department) { this.department = department; }

    /** Retourne le prénom. */
    public String getName() { return name; }
    /** Définit le prénom. */
    public void setName(String name) { this.name = name; }

    /** Retourne le nom de famille. */
    public String getSurname() { return surname; }
    /** Définit le nom de famille. */
    public void setSurname(String surname) { this.surname = surname; }

    /** Retourne le statut. */
    public Status getStatus() { return status; }
    /** Définit le statut. */
    public void setStatus(Status status) { this.status = status; }

    /** Retourne le planning. */
    public Schedule getSchedule() { return schedule; }
    /** Définit le planning. */
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    /** Retourne le solde en minutes. */
    public long getSoldeMinutes() { return soldeMinutes; }
    /** Définit le solde en minutes. */
    public void setSoldeMinutes(long soldeMinutes) { this.soldeMinutes = soldeMinutes; }

    /**
     * Représentation lisible de l'employé.
     * @return chaîne contenant prénom, nom et département
     */
    @Override
    public String toString() {
        String deptName = (department != null) ? department.getName() : "Aucun";
        return name + " " + surname + " [" + deptName + "]";
    }

    /**
     * Égalité basée sur l'identifiant unique.
     * @param object autre objet
     * @return true si mêmes identifiants
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass()) return false;
        Employee emp = (Employee) object;
        return id.equals(emp.id);
    }

    /** Retourne le hash code basé sur l'identifiant. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Copie les données d'un autre employé dans cet objet.
     * @param employee source
     */
    public void setEmployee(Employee employee) {
        if (employee != null) {
            this.id = employee.id;
            this.department = employee.department;
            this.name = employee.name;
            this.surname = employee.surname;
            this.status = employee.status;
            this.schedule = employee.schedule;
            this.soldeMinutes = employee.soldeMinutes;
        }
    }
}