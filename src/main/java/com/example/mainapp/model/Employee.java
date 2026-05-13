package com.example.mainapp.model;

import com.example.mainapp.enums.Status;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

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

    public Employee() {
        this.id = UUID.randomUUID();
        this.name = "";
        this.surname = "";
        this.status = Status.EMP;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    public Employee(Department department, String name, String surname, Status status) {
        this.id = UUID.randomUUID();
        this.department = department;
        this.name = name;
        this.surname = surname;
        this.status = status;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    // ==========================================
    // MÉTHODES MÉTIER
    // ==========================================

    public void modifierSoldeMinutes(long minutes) {
        this.soldeMinutes += minutes;
    }

    // ==========================================
    // GETTERS & SETTERS COMPLETS
    // ==========================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Schedule getSchedule() { return schedule; }
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    public long getSoldeMinutes() { return soldeMinutes; }
    public void setSoldeMinutes(long soldeMinutes) { this.soldeMinutes = soldeMinutes; }

    // ==========================================
    // MÉTHODES REDÉFINIES
    // ==========================================

    @Override
    public String toString() {
        String deptName = (department != null) ? department.getName() : "Aucun";
        return name + " " + surname + " [" + deptName + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass()) return false;

        Employee emp = (Employee) object;
        return id.equals(emp.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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