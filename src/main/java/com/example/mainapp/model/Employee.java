package com.example.mainapp.model;

import com.example.mainapp.enums.Statue;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String dept;
    private String name;
    private String surname;
    private Statue status;

    public Employee() {
        id = UUID.randomUUID();
        dept = "";
        name = "";
        surname = "";
        status = Statue.EMP;
    }

    public Employee(String dept, String name, String surname, Statue status) {
        id = UUID.randomUUID();
        this.dept = dept;
        this.name = name;
        this.surname = surname;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getDept() {
        return dept;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setStatus(Statue status) {
        this.status = status;
    }

    public Statue getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return id + ", " + name + ", " + surname + ", " + dept + ", " + status;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass()) return false;

        Employee emp = (Employee) object;

        return id.equals(emp.id) && name.equals(emp.name) && surname.equals(emp.surname);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id) + name.hashCode() + surname.hashCode();
    }

    public void setEmployee(Employee employee) {
        this.id = employee.id;
        this.dept = employee.dept;
        this.name = employee.name;
        this.surname = employee.surname;
        this.status = employee.status;
    }
}
