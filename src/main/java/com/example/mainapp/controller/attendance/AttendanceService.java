package com.example.mainapp.controller.attendance;

import com.example.mainapp.model.AttendanceRecord;
import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.mainapp.model.TimeSlot;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.ConfigManager;
import com.example.mainapp.utils.PersistenceManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceService {

    private static AttendanceService instance;
    private final Company company;

    private AttendanceService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    public static AttendanceService getInstance() {
        if (instance == null) {
            instance = new AttendanceService();
        }
        return instance;
    }

    /**
     * @brief Evaluates the record to assign a descriptive status string.
     * @details Modifies the record's status text but leaves the mathematical balance calculation
     * to the recalculateBalance method to prevent duplicate operations.
     * @param record The attendance record to evaluate
     */
    public void evaluerEtAppliquerPointage(AttendanceRecord record) {
        Employee emp = record.getEmployee();

        if (emp != null && emp.getSchedule() != null) {
            DayOfWeek jour = record.getTime().getDayOfWeek();
            TimeSlot slot = emp.getSchedule().getHorairePourJour(jour);

            if (slot != null && slot.getArrivee() != null && slot.getDepart() != null) {
                ConfigManager config = new ConfigManager();
                int tolerance = config.getToleranceMinutes();

                LocalTime actualTime = record.getTime().toLocalTime();
                long diffMinutes = 0;

                if (record.isCheckIn()) {
                    LocalTime scheduledTime = slot.getArrivee();
                    diffMinutes = Duration.between(scheduledTime, actualTime).toMinutes();

                    if (diffMinutes > tolerance) {
                        record.setStatus("Incident : Retard");
                    } else if (diffMinutes < -tolerance) {
                        record.setStatus("Avance");
                    } else {
                        record.setStatus("Normal");
                    }
                } else {
                    LocalTime scheduledTime = slot.getDepart();
                    diffMinutes = Duration.between(scheduledTime, actualTime).toMinutes();

                    if (diffMinutes > tolerance) {
                        record.setStatus("Heures supp.");
                    } else if (diffMinutes < -tolerance) {
                        record.setStatus("Incident : Départ anticipé");
                    } else {
                        record.setStatus("Normal");
                    }
                }
            } else {
                record.setStatus("Incident : Hors planning");
            }
        } else {
            record.setStatus("Normal (Aucun planning assigné)");
        }
    }

    /**
     * @brief Recalcule le solde total d'un employé en traitant son historique.
     * @details Groupe les pointages par jour, associe les entrées et sorties, nettoie les anciens statuts d'erreur corrigés, ignore les journées en cours, et applique la tolérance.
     * @param emp L'employé dont le solde doit être recalculé.
     * @param attendanceHistory L'historique complet des pointages de l'employé.
     */
    public void recalculateBalance(Employee emp, List<AttendanceRecord> attendanceHistory) {
        emp.setSoldeMinutes(0L);

        if (attendanceHistory == null || attendanceHistory.isEmpty()) {
            return;
        }

        // Tri chronologique
        attendanceHistory.sort(Comparator.comparing(AttendanceRecord::getTime));

        // Regroupement par jour
        Map<LocalDate, List<AttendanceRecord>> recordsByDay = attendanceHistory.stream()
                .collect(Collectors.groupingBy(r -> r.getTime().toLocalDate()));

        long newBalance = 0L;
        ConfigManager config = new ConfigManager();
        int tolerance = config.getToleranceMinutes();

        LocalDate today = LocalDate.now();

        // Analyse jour par jour
        for (Map.Entry<LocalDate, List<AttendanceRecord>> entry : recordsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            List<AttendanceRecord> dailyRecords = entry.getValue();

            long workedMinutesToday = 0L;
            boolean missingCheckoutAnomaly = false;
            boolean shiftInProgress = false;

            // Boucle d'association Entrée/Sortie
            for (int i = 0; i < dailyRecords.size(); i++) {
                AttendanceRecord current = dailyRecords.get(i);

                if (current.isCheckIn()) {
                    if (i + 1 < dailyRecords.size() && !dailyRecords.get(i + 1).isCheckIn()) {
                        AttendanceRecord checkOut = dailyRecords.get(i + 1);
                        workedMinutesToday += Duration.between(current.getTime(), checkOut.getTime()).toMinutes();

                        // Nettoyage : Si on trouve la paire, on efface l'erreur "Sortie manquante" de l'entrée
                        if (current.getStatus() != null && current.getStatus().contains("Sortie manquante")) {
                            evaluerEtAppliquerPointage(current);
                        }

                        i++; // On saute la sortie puisqu'elle est traitée
                    } else {
                        // Gestion des oublis vs Journée en cours
                        if (date.isBefore(today)) {
                            missingCheckoutAnomaly = true;
                            current.setStatus("Incident : Sortie manquante");
                        } else {
                            shiftInProgress = true;
                        }
                    }
                }
            }

            // Récupération des heures attendues
            long expectedMinutes = 0L;
            if (emp.getSchedule() != null) {
                expectedMinutes = emp.getSchedule().getMinutesPourCeJour(date.getDayOfWeek());
            }

            // Application des règles sur le solde
            if (missingCheckoutAnomaly) {
                newBalance -= expectedMinutes;
            } else if (!shiftInProgress) {
                long rawDifference = workedMinutesToday - expectedMinutes;

                if (Math.abs(rawDifference) <= tolerance) {
                    rawDifference = 0;
                }

                newBalance += rawDifference;
            }
        }

        emp.setSoldeMinutes(newBalance);
    }

    public void addAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || record.getEmployee() == null) {
            throw new Exception("Le pointage est invalide.");
        }

        evaluerEtAppliquerPointage(record);
        company.getAttendanceRecords().add(record);

        // Recalculate balance for this specific employee
        Employee emp = record.getEmployee();
        List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

        recalculateBalance(emp, employeeHistory);

        saveData();
    }

    public void updateAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null) throw new Exception("Le pointage à modifier est invalide.");

        evaluerEtAppliquerPointage(record);
        record.setStatus(record.getStatus() + " (Modifié)");

        // Recalculate balance for this specific employee
        Employee emp = record.getEmployee();
        List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

        recalculateBalance(emp, employeeHistory);

        saveData();
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return company.getAttendanceRecords();
    }

    public void deleteAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || !company.getAttendanceRecords().remove(record)) {
            throw new Exception("Suppression impossible.");
        }

        // Note: It's good practice to recalculate the balance after a deletion too
        Employee emp = record.getEmployee();
        if (emp != null) {
            List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                    .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                    .collect(Collectors.toList());
            recalculateBalance(emp, employeeHistory);
        }

        saveData();
    }

    private void saveData() {
        try {
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }
}