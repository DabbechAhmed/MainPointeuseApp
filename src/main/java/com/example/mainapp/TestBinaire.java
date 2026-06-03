package com.example.mainapp;

import com.example.mainapp.model.*;
import com.example.mainapp.enums.Status;
import com.example.mainapp.utils.PersistenceManager;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TestBinaire {

    public static void main(String[] args) {

        System.out.println("=== 1. PRÉPARATION DES DONNÉES ===");

        Company maCompagnie = new Company("Polytech Tours");

        // ==========================================
        // CRÉATION DES DÉPARTEMENTS
        // ==========================================
        Department deptIT = new Department("Informatique");
        Department deptRH = new Department("Ressources Humaines");
        Department deptAccounting = new Department("Comptabilité");
        Department deptLogistics = new Department("Logistique");

        // ==========================================
        // CRÉATION DES EMPLOYÉS - DÉPARTEMENT IT
        // ==========================================
        Employee emp1 = new Employee(deptIT, "M'SADAA", "Youssef", Status.EMP);
        Employee emp2 = new Employee(deptIT, "DEBBACH", "Ahmed", Status.EMP);
        Employee emp3 = new Employee(deptIT, "RIANI", "Youssef", Status.EMP);

        configurerScheduleIT(emp1);
        configurerScheduleIT(emp2);
        configurerScheduleIT(emp3);

        // ==========================================
        // CRÉATION DES EMPLOYÉS - DÉPARTEMENT RH
        // ==========================================
        Employee emp4 = new Employee(deptRH, "BEN ABDA", "Mohamed Yassine", Status.HRR);
        Employee emp5 = new Employee(deptRH, "SOPHIE", "Laurent", Status.EMP);

        configurerScheduleRH(emp4);
        configurerScheduleRH(emp5);

        // ==========================================
        // CRÉATION DES EMPLOYÉS - DÉPARTEMENT COMPTABILITÉ
        // ==========================================
        Employee emp6 = new Employee(deptAccounting, "MARTIN", "Philippe", Status.EMP);
        Employee emp7 = new Employee(deptAccounting, "DURAND", "Marie", Status.EMP);

        configurerScheduleAccounting(emp6);
        configurerScheduleAccounting(emp7);

        // ==========================================
        // CRÉATION DES EMPLOYÉS - DÉPARTEMENT LOGISTIQUE
        // ==========================================
        Employee emp8 = new Employee(deptLogistics, "BERNARD", "Pascal", Status.EMP);
        Employee emp9 = new Employee(deptLogistics, "THOMAS", "Nicolas", Status.EMP);
        Employee emp10 = new Employee(deptLogistics, "EL YAHYAOUI", "Youssef", Status.EMP);

        configurerScheduleLogistics(emp8);
        configurerScheduleLogistics(emp9);
        configurerScheduleLogistics(emp10);

        // ==========================================
        // ÉTABLISSEMENT DES LIENS (Départements & Employés)
        // ==========================================
        maCompagnie.addDepartment(deptIT);
        maCompagnie.addDepartment(deptRH);
        maCompagnie.addDepartment(deptAccounting);
        maCompagnie.addDepartment(deptLogistics);

        Employee[] tousEmployes = {emp1, emp2, emp3, emp4, emp5, emp6, emp7, emp8, emp9, emp10};
        for (Employee emp : tousEmployes) {
            maCompagnie.addEmployee(emp);
        }

        deptIT.addEmployee(emp1);
        deptIT.addEmployee(emp2);
        deptIT.addEmployee(emp3);
        deptRH.addEmployee(emp4);
        deptRH.addEmployee(emp5);
        deptAccounting.addEmployee(emp6);
        deptAccounting.addEmployee(emp7);
        deptLogistics.addEmployee(emp8);
        deptLogistics.addEmployee(emp9);
        deptLogistics.addEmployee(emp10);

        // ==========================================
        // ✅ NOUVEAU : CRÉATION DE POINTAGES DE TEST
        // ==========================================
        LocalDateTime aujourdhui = LocalDateTime.now();

        // Ahmed arrive à 8h55 (Avance) et repart à 18h05
        AttendanceRecord att1 = new AttendanceRecord(emp2, aujourdhui.withHour(8).withMinute(55), true);
        AttendanceRecord att2 = new AttendanceRecord(emp2, aujourdhui.withHour(18).withMinute(5), false);

        // Mohamed Yassine arrive à 9h15 (Retard)
        AttendanceRecord att3 = new AttendanceRecord(emp4, aujourdhui.withHour(9).withMinute(15), true);

        // Youssef Riani arrive à 7h25
        AttendanceRecord att4 = new AttendanceRecord(emp3, aujourdhui.withHour(7).withMinute(25), true);

        maCompagnie.addAttendanceRecord(att1);
        maCompagnie.addAttendanceRecord(att2);
        maCompagnie.addAttendanceRecord(att3);
        maCompagnie.addAttendanceRecord(att4);

        System.out.println("✅ Données générées avec succès (10 employés, 4 départements, 4 pointages).");

        System.out.println("\n=== 2. TEST DE SAUVEGARDE ===");
        PersistenceManager.saveData(maCompagnie);

        System.out.println("\n=== 3. TEST DE CHARGEMENT ===");
        Company compagnieChargee = PersistenceManager.loadData();

        System.out.println("\n=== 4. VÉRIFICATION DE L'INTÉGRITÉ ===");
        if (compagnieChargee != null && !compagnieChargee.getEmployees().isEmpty()) {

            System.out.println("🏢 Entreprise : " + compagnieChargee.getName());
            System.out.println("🏭 Départements : " + compagnieChargee.getDepartments().size());
            System.out.println("👥 Employés : " + compagnieChargee.getEmployees().size());

            // ✅ Vérification des pointages
            System.out.println("🕒 Pointages récupérés : " + compagnieChargee.getAttendanceRecords().size());
            System.out.println("-------------------------------------------------");

            for (AttendanceRecord recup : compagnieChargee.getAttendanceRecords()) {
                String type = recup.isCheckIn() ? "ENTRÉE" : "SORTIE";
                System.out.println("📍 " + recup.getEmployee().getName() + " | " + type + " à " + recup.getTime().toLocalTime());
            }
            System.out.println("-------------------------------------------------");
        }
    }

    // ==========================================
    // MÉTHODES AUXILIAIRES - CONFIGURATION DES SCHEDULES
    // ==========================================
    private static void configurerScheduleIT(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("M'SADAA")) { slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(17, 0)); }
        else if (emp.getName().equals("DEBBACH")) { slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(18, 0)); }
        else { slot = new TimeSlot(LocalTime.of(7, 30), LocalTime.of(16, 30)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    private static void configurerScheduleRH(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getStatus() == Status.HRR) { slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 30)); }
        else { slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    private static void configurerScheduleAccounting(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("MARTIN")) { slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 30)); }
        else { slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    private static void configurerScheduleLogistics(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("BERNARD")) { slot = new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0)); }
        else if (emp.getName().equals("THOMAS")) { slot = new TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0)); }
        else { slot = new TimeSlot(LocalTime.of(7, 0), LocalTime.of(15, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }
}