package com.example.mainapp.model;

import com.example.mainapp.enums.Status;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    // MÉTHODES MÉTIER (LA CORRECTION)
    // ==========================================

    public void modifierSoldeMinutes(long minutes) {
        this.soldeMinutes += minutes;
    }

    /**
     * Recalcule le solde à partir de zéro pour éviter les désynchronisations.
     */
    public void recalculerSolde(List<AttendanceRecord> historiquePointages) {
        // 1. On remet les compteurs à zéro
        this.soldeMinutes = 0L;

        if (historiquePointages == null || historiquePointages.isEmpty()) {
            return;
        }

        // 2. On trie les pointages du plus ancien au plus récent
        historiquePointages.sort(Comparator.comparing(AttendanceRecord::getTime));

        // 3. On regroupe les pointages par jour (LocalDate)
        Map<LocalDate, List<AttendanceRecord>> pointagesParJour = historiquePointages.stream()
                .collect(Collectors.groupingBy(r -> r.getTime().toLocalDate()));

        // 4. On calcule le solde jour par jour
        // Dans ta méthode recalculerSolde(List<AttendanceRecord> historiquePointages) :

        for (Map.Entry<LocalDate, List<AttendanceRecord>> entry : pointagesParJour.entrySet()) {
            LocalDate jour = entry.getKey();
            List<AttendanceRecord> pointagesDuJour = entry.getValue();

            // 1. On calcule le temps réellement travaillé ce jour-là
            long minutesTravailleesCeJour = 0L;
            for (int i = 0; i < pointagesDuJour.size() - 1; i += 2) {
                AttendanceRecord entree = pointagesDuJour.get(i);
                AttendanceRecord sortie = pointagesDuJour.get(i + 1);
                if (entree.isCheckIn() && !sortie.isCheckIn()) {
                    minutesTravailleesCeJour += Duration.between(entree.getTime(), sortie.getTime()).toMinutes();
                }
            }

            // 2. LA VERSION DYNAMIQUE : On demande à l'objet Schedule de l'employé
            // combien d'heures il est censé faire ce jour spécifique (ex: lundi, mardi...)
            long minutesAttendues = this.schedule.getMinutesPourCeJour(jour.getDayOfWeek());

            // 3. On met à jour le solde (différence réelle)
            this.soldeMinutes += (minutesTravailleesCeJour - minutesAttendues);
        }
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