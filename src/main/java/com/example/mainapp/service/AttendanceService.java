package com.example.mainapp.service;

import com.example.mainapp.model.AttendanceRecord;
import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.mainapp.model.TimeSlot;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.ConfigManager;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    // ==========================================
    // 🧠 LOGIQUE MÉTIER CENTRALISÉE
    // ==========================================
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
                        emp.setSoldeMinutes(emp.getSoldeMinutes() - diffMinutes);
                    } else if (diffMinutes < -tolerance) {
                        record.setStatus("Avance");
                        emp.setSoldeMinutes(emp.getSoldeMinutes() + Math.abs(diffMinutes));
                    } else {
                        record.setStatus("Normal");
                    }
                } else {
                    LocalTime scheduledTime = slot.getDepart();
                    diffMinutes = Duration.between(scheduledTime, actualTime).toMinutes();

                    if (diffMinutes > tolerance) {
                        record.setStatus("Heures supp.");
                        emp.setSoldeMinutes(emp.getSoldeMinutes() + diffMinutes);
                    } else if (diffMinutes < -tolerance) {
                        record.setStatus("Incident : Départ anticipé");
                        emp.setSoldeMinutes(emp.getSoldeMinutes() + diffMinutes); // diff est négatif
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

    public void addAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || record.getEmployee() == null) {
            throw new Exception("Le pointage est invalide.");
        }

        evaluerEtAppliquerPointage(record);

        company.getAttendanceRecords().add(record);
        saveData();
    }

    // ==========================================
    // 🟧 UPDATE (Mettre à jour)
    // ==========================================
    public void updateAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null) throw new Exception("Le pointage à modifier est invalide.");

        // 1. Mise à jour de l'état du pointage spécifique
        evaluerEtAppliquerPointage(record);
        record.setStatus(record.getStatus() + " (Modifié)");

        // 2. RECALCUL COMPLET DU SOLDE DE L'EMPLOYÉ
        Employee emp = record.getEmployee();


        List<AttendanceRecord> historiqueComplet = this.company.getAttendanceRecords().stream()
                .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                .collect(java.util.stream.Collectors.toList());

        // On lance le recalcul intelligent
        emp.recalculerSolde(historiqueComplet);

        // 3. Sauvegarde de l'état global
        saveData();
    }
    public List<AttendanceRecord> getAllAttendanceRecords() { return company.getAttendanceRecords(); }

    public void deleteAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || !company.getAttendanceRecords().remove(record)) {
            throw new Exception("Suppression impossible.");
        }
        saveData();
    }

    private void saveData() {
        try {
            com.example.mainapp.service.PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }
}