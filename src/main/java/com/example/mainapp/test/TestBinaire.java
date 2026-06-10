package com.example.mainapp.test;

import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.model.employee.Status;
import com.example.mainapp.model.schedule.Schedule;
import com.example.mainapp.model.schedule.TimeSlot;
import com.example.mainapp.utils.PersistenceManager;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @brief Script de génération du jeu d'essai initial (Data Seeder).
 * <p>
 * Cette classe utilitaire permet de réinitialiser l'environnement de test en
 * générant une entreprise complète avec ses départements, ses employés,
 * leurs horaires spécifiques, et un jeu de pointages cohérent.
 * Elle valide également le bon fonctionnement du PersistenceManager.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class TestBinaire {

    /**
     * @brief Point d'entrée du générateur de données.
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {

        System.out.println("=== 1. PRÉPARATION DES DONNÉES ===");

        Company maCompagnie = new Company("Polytech Tours");

        Department deptIT = new Department("Informatique");
        Department deptRH = new Department("Ressources Humaines");
        Department deptAccounting = new Department("Comptabilité");
        Department deptLogistics = new Department("Logistique");

        Employee emp1 = new Employee(deptIT, "M'SADAA", "Youssef", Status.EMP);
        Employee emp2 = new Employee(deptIT, "DEBBACH", "Ahmed", Status.EMP);
        Employee emp3 = new Employee(deptIT, "RIANI", "Youssef", Status.EMP);

        configurerScheduleIT(emp1);
        configurerScheduleIT(emp2);
        configurerScheduleIT(emp3);


        Employee emp4 = new Employee(deptRH, "BEN ABDA", "Mohamed Yassine", Status.HRR);
        Employee emp5 = new Employee(deptRH, "SOPHIE", "Laurent", Status.EMP);

        configurerScheduleRH(emp4);
        configurerScheduleRH(emp5);

        Employee emp6 = new Employee(deptAccounting, "MARTIN", "Philippe", Status.EMP);
        Employee emp7 = new Employee(deptAccounting, "DURAND", "Marie", Status.EMP);

        configurerScheduleAccounting(emp6);
        configurerScheduleAccounting(emp7);

        Employee emp8 = new Employee(deptLogistics, "BERNARD", "Pascal", Status.EMP);
        Employee emp9 = new Employee(deptLogistics, "THOMAS", "Nicolas", Status.EMP);
        Employee emp10 = new Employee(deptLogistics, "EL YAHYAOUI", "Youssef", Status.EMP);

        configurerScheduleLogistics(emp8);
        configurerScheduleLogistics(emp9);
        configurerScheduleLogistics(emp10);

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

        // Nettoyage des secondes pour un affichage IHM propre
        LocalDateTime aujourdhui = LocalDateTime.now().withSecond(0).withNano(0);

        // Scénario 1 : DEBBACH (Horaire prévu : 09:00 - 18:00) -> Arrivée en avance, départ en retard
        AttendanceRecord att1 = new AttendanceRecord(emp2, aujourdhui.withHour(8).withMinute(55), true);
        AttendanceRecord att2 = new AttendanceRecord(emp2, aujourdhui.withHour(18).withMinute(5), false);

        // Scénario 2 : BEN ABDA (Horaire prévu : 08:30 - 17:30) -> Ponctualité parfaite
        AttendanceRecord att3 = new AttendanceRecord(emp4, aujourdhui.withHour(8).withMinute(25), true);
        AttendanceRecord att4 = new AttendanceRecord(emp4, aujourdhui.withHour(17).withMinute(30), false);

        // Scénario 3 : RIANI (Horaire prévu : 07:30 - 16:30) -> Retard le matin (pour tester la tolérance)
        AttendanceRecord att5 = new AttendanceRecord(emp3, aujourdhui.withHour(7).withMinute(45), true);

        maCompagnie.addAttendanceRecord(att1);
        maCompagnie.addAttendanceRecord(att2);
        maCompagnie.addAttendanceRecord(att3);
        maCompagnie.addAttendanceRecord(att4);
        maCompagnie.addAttendanceRecord(att5);

        System.out.println("Données générées avec succès (10 employés, 4 départements, 5 pointages).");

        System.out.println("\n=== 2. TEST DE SAUVEGARDE ===");
        PersistenceManager.saveData(maCompagnie);

        System.out.println("\n=== 3. TEST DE CHARGEMENT ===");
        Company compagnieChargee = PersistenceManager.loadData();

        System.out.println("\n=== 4. VÉRIFICATION DE L'INTÉGRITÉ ===");
        if (compagnieChargee != null && !compagnieChargee.getEmployees().isEmpty()) {

            System.out.println("Entreprise : " + compagnieChargee.getName());
            System.out.println("Départements : " + compagnieChargee.getDepartments().size());
            System.out.println("Employés : " + compagnieChargee.getEmployees().size());

            System.out.println("Pointages récupérés : " + compagnieChargee.getAttendanceRecords().size());
            System.out.println("-------------------------------------------------");

            for (AttendanceRecord recup : compagnieChargee.getAttendanceRecords()) {
                String type = recup.isCheckIn() ? "ENTRÉE" : "SORTIE";
                System.out.println(recup.getEmployee().getName() + " | " + type + " à " + recup.getTime().toLocalTime());
            }
            System.out.println("-------------------------------------------------");
        }
    }

    /**
     * @brief Configure les plannings par défaut pour le département Informatique.
     * @param emp Employé ciblé.
     */
    private static void configurerScheduleIT(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("M'SADAA")) { slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(17, 0)); }
        else if (emp.getName().equals("DEBBACH")) { slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(18, 0)); }
        else { slot = new TimeSlot(LocalTime.of(7, 30), LocalTime.of(16, 30)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    /**
     * @brief Configure les plannings par défaut pour le département Ressources Humaines.
     * @param emp Employé ciblé.
     */
    private static void configurerScheduleRH(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getStatus() == Status.HRR) { slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 30)); }
        else { slot = new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    /**
     * @brief Configure les plannings par défaut pour le département Comptabilité.
     * @param emp Employé ciblé.
     */
    private static void configurerScheduleAccounting(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("MARTIN")) { slot = new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 30)); }
        else { slot = new TimeSlot(LocalTime.of(8, 30), LocalTime.of(17, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }

    /**
     * @brief Configure les plannings par défaut pour le département Logistique.
     * @param emp Employé ciblé.
     */
    private static void configurerScheduleLogistics(Employee emp) {
        Schedule schedule = emp.getSchedule();
        TimeSlot slot;
        if (emp.getName().equals("BERNARD")) { slot = new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0)); }
        else if (emp.getName().equals("THOMAS")) { slot = new TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0)); }
        else { slot = new TimeSlot(LocalTime.of(7, 0), LocalTime.of(15, 0)); }
        for (DayOfWeek day : DayOfWeek.values()) schedule.definirJournee(day, slot);
    }
}