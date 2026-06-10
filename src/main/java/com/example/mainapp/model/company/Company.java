package com.example.mainapp.model.company;

import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.model.employee.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Représente une entreprise contenant des employés, des départements
 * et des enregistrements de pointage.
 */
public class Company implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Nom de l'entreprise */
    private String name;

    /** Liste des employés de l'entreprise */
    private final List<Employee> employees;

    /** Liste des départements de l'entreprise */
    private final List<Department> departments;

    /** Liste des enregistrements de pointage */
    private final List<AttendanceRecord> attendanceRecords;

    /**
     * Crée une nouvelle entreprise avec un nom et des listes initialisées.
     * @param name Nom de l'entreprise
     */
    public Company(String name) {
        this.name = name;
        this.employees = new ArrayList<>();
        this.departments = new ArrayList<>();
        this.attendanceRecords = new ArrayList<>();
    }

    /**
     * Ajoute un enregistrement de pointage si non nul.
     * @param record enregistrement à ajouter
     */
    public void addAttendanceRecord(AttendanceRecord record) {
        if (record != null) {
            this.attendanceRecords.add(record);
        }
    }

    /**
     * Retourne la liste des enregistrements de pointage.
     * @return liste des AttendanceRecord
     */
    public List<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    /**
     * Ajoute un employé s'il n'est pas nul et si son ID n'est pas déjà présent.
     * @param employee employé à ajouter
     */
    public void addEmployee(Employee employee) {
        if (employee != null && findEmployeeById(employee.getId()) == null) {
            this.employees.add(employee);
        }
    }

    /**
     * Supprime un employé par identifiant.
     * @param id identifiant de l'employé
     */
    public void removeEmployee(UUID id) {
        if (id != null) {
            this.employees.removeIf(emp -> emp.getId().equals(id));
        }
    }

    /**
     * Recherche un employé par identifiant.
     * @param id identifiant recherché
     * @return l'employé si trouvé, sinon null
     */
    public Employee findEmployeeById(UUID id) {
        if (id == null) return null;
        for (Employee emp : employees) {
            if (emp.getId().equals(id)) {
                return emp;
            }
        }
        return null;
    }

    /**
     * Ajoute un département s'il n'est pas nul et n'est pas déjà présent.
     * @param dept département à ajouter
     */
    public void addDepartment(Department dept) {
        if (dept != null && !this.departments.contains(dept)) {
            this.departments.add(dept);
        }
    }

    /**
     * Supprime un département par identifiant.
     * @param id identifiant du département
     */
    public void removeDepartment(UUID id) {
        if (id != null) {
            this.departments.removeIf(dept -> dept.getId().equals(id));
        }
    }

    /**
     * Retourne le nom de l'entreprise.
     * @return le nom de l'entreprise
     */
    public String getName() {
        return name;
    }

    /**
     * Modifie le nom de l'entreprise.
     * @param name le nouveau nom de l'entreprise
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne la liste des employés de l'entreprise.
     * @return la liste des instances d'employés
     */
    public List<Employee> getEmployees() {
        return employees;
    }

    /**
     * Retourne la liste des départements de l'entreprise.
     * @return la liste des instances de départements
     */
    public List<Department> getDepartments() {
        return departments;
    }
}