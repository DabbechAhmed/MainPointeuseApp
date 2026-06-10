package com.example.mainapp.controller.attendance;

import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.model.schedule.TimeSlot;
import com.example.mainapp.network.TCPServer;
import com.example.mainapp.utils.ConfigManager;
import com.example.mainapp.utils.PersistenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @brief Service métier centralisé gérant l'évaluation des pointages, le calcul des soldes et les imports de données.
 * <p>
 * Cette classe implémente le pattern Singleton et centralise le cœur algorithmique de gestion du temps de l'application.
 * </p>
 */
public class AttendanceService {

    /** L'instance unique (Singleton) du service de gestion des pointages. */
    private static AttendanceService instance;

    /** La référence vers l'entité globale de l'entreprise pour manipuler le modèle de données. */
    private final Company company;

    /**
     * @brief Constructeur privé extrayant l'instance de l'entreprise depuis le serveur TCP actif.
     */
    private AttendanceService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    /**
     * @brief Retourne l'instance unique et globale de ce service.
     * @return L'instance unique de {@link AttendanceService}.
     */
    public static AttendanceService getInstance() {
        if (instance == null) {
            instance = new AttendanceService();
        }
        return instance;
    }

    /**
     * @brief Qualifie et applique le statut de conformité d'un enregistrement de pointage unique.
     * @param record L'enregistrement de pointage à analyser et qualifier.
     */
    public void evaluateAndApplyAttendance(AttendanceRecord record) {
        if (company.getAttendanceRecords() != null) {
            for (AttendanceRecord existant : company.getAttendanceRecords()) {
                if (existant != record &&
                        existant.getEmployee().getId().equals(record.getEmployee().getId()) &&
                        existant.isCheckIn() == record.isCheckIn() &&
                        existant.getTime().toLocalDate().equals(record.getTime().toLocalDate())) {

                    record.setStatus("Incident : Doublon");
                    System.out.println("ALERTE : Double pointage détecté pour " + record.getEmployee().getName() + " !");
                    return;
                }
            }
        }

        Employee employee = record.getEmployee();

        if (employee != null && employee.getSchedule() != null) {
            DayOfWeek day = record.getTime().getDayOfWeek();
            TimeSlot slot = employee.getSchedule().getHorairePourJour(day);

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
     * @brief Recalcule l'intégralité du solde de minutes d'un employé à partir de son historique chronologique.
     * <p>
     * L'algorithme opère un scan chronologique absolu pour lier les entrées et sorties, même d'un jour à l'autre.
     * Si une entrée est suivie d'une autre entrée, la première est marquée en "Sortie manquante".
     * Il convertit ensuite les paires valides en temps de travail effectif.
     * </p>
     *
     * @param employee          L'employé dont le solde doit être mis à jour.
     * @param attendanceHistory La liste complète de tous les pointages historiques de cet employé.
     */
    public void recalculateBalance(Employee employee, List<AttendanceRecord> attendanceHistory) {
        employee.setSoldeMinutes(0L);

        if (attendanceHistory == null || attendanceHistory.isEmpty()) {
            return;
        }

        // 1. Tri chronologique absolu de tout l'historique
        attendanceHistory.sort(Comparator.comparing(AttendanceRecord::getTime));
        LocalDate today = LocalDate.now();

        // 2. Scan global pour détecter les anomalies logiques
        for (int i = 0; i < attendanceHistory.size(); i++) {
            AttendanceRecord current = attendanceHistory.get(i);

            if (current.isCheckIn()) {
                if (i + 1 < attendanceHistory.size()) {
                    AttendanceRecord next = attendanceHistory.get(i + 1);

                    if (next.isCheckIn()) {
                        // L'employé a refait une "Entrée" sans faire de "Sortie" avant.
                        current.setStatus("Incident : Sortie manquante");
                    } else {
                        // C'est bien une "Sortie". A-t-elle été faite le même jour ?
                        if (!current.getTime().toLocalDate().equals(next.getTime().toLocalDate())) {
                            current.setStatus("Incident : Sortie manquante");
                        } else {
                            // Paire parfaite. Si c'était une vieille erreur corrigée, on restaure le statut.
                            if (current.getStatus() != null && current.getStatus().contains("Sortie manquante")) {
                                evaluateAndApplyAttendance(current);
                            }
                            i++; // On saute la "Sortie" car elle est validée
                        }
                    }
                } else {
                    // C'est le tout dernier pointage de l'historique
                    if (current.getTime().toLocalDate().isBefore(today)) {
                        current.setStatus("Incident : Sortie manquante");
                    }
                }
            } else {
                // Une "Sortie" trouvée sans "Entrée" préalable (car les paires sont sautées par i++)
                current.setStatus("Incident : Entrée manquante");
            }
        }

        // 3. Calcul du solde par blocs journaliers
        Map<LocalDate, List<AttendanceRecord>> recordsByDay = attendanceHistory.stream()
                .collect(Collectors.groupingBy(r -> r.getTime().toLocalDate()));

        long newBalance = 0L;
        ConfigManager config = new ConfigManager();
        int tolerance = config.getToleranceMinutes();

        for (Map.Entry<LocalDate, List<AttendanceRecord>> entry : recordsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            List<AttendanceRecord> dailyRecords = entry.getValue();

            long workedMinutesToday = 0L;
            boolean shiftInProgress = false;

            for (int i = 0; i < dailyRecords.size(); i++) {
                AttendanceRecord current = dailyRecords.get(i);

                if (current.isCheckIn()) {
                    if (i + 1 < dailyRecords.size() && !dailyRecords.get(i + 1).isCheckIn()) {
                        workedMinutesToday += Duration.between(current.getTime(), dailyRecords.get(i + 1).getTime()).toMinutes();
                        i++;
                    } else if (!current.getStatus().contains("Sortie manquante")) {
                        // L'employé est actuellement en train de travailler (aujourd'hui)
                        shiftInProgress = true;
                    }
                }
            }

            long expectedMinutes = 0L;
            if (employee.getSchedule() != null) {
                expectedMinutes = employee.getSchedule().getMinutesPourCeJour(date.getDayOfWeek());
            }

            if (!shiftInProgress) {
                long rawDifference = workedMinutesToday - expectedMinutes;
                if (Math.abs(rawDifference) <= tolerance) {
                    rawDifference = 0;
                }
                newBalance += rawDifference;
            }
        }

        employee.setSoldeMinutes(newBalance);
    }

    /**
     * @brief Ajoute un nouveau pointage au modèle et recalcule le solde de l'employé associé.
     */
    public void addAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || record.getEmployee() == null) {
            throw new Exception("Le pointage est invalide.");
        }

        evaluateAndApplyAttendance(record);
        company.getAttendanceRecords().add(record);

        Employee emp = record.getEmployee();
        List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

        recalculateBalance(emp, employeeHistory);
        saveData();
    }

    /**
     * @brief Traite et réévalue un pointage existant suite à une modification ou correction manuelle.
     */
    public void updateAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null) throw new Exception("Le pointage à modifier est invalide.");

        evaluateAndApplyAttendance(record);
        record.setStatus(record.getStatus() + " (Modifié)");

        Employee emp = record.getEmployee();
        List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                .collect(Collectors.toList());

        recalculateBalance(emp, employeeHistory);
        saveData();
    }

    /**
     * @brief Récupère la liste brute de tous les enregistrements de pointage.
     */
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return company.getAttendanceRecords();
    }

    /**
     * @brief Supprime définitivement un enregistrement de pointage du modèle central de données.
     */
    public void deleteAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || !company.getAttendanceRecords().remove(record)) {
            throw new Exception("Suppression impossible.");
        }

        Employee emp = record.getEmployee();
        if (emp != null) {
            List<AttendanceRecord> employeeHistory = this.company.getAttendanceRecords().stream()
                    .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(emp.getId()))
                    .collect(Collectors.toList());
            recalculateBalance(emp, employeeHistory);
        }

        saveData();
    }

    /**
     * @brief Importe de manière massive un flux de pointages à partir d'un fichier CSV externe.
     */
    public void importRecordsFromCSV(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new Exception("Le fichier CSV est invalide ou introuvable.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(";");
                if (data.length < 3) {
                    continue;
                }

                String empId = data[0].trim();
                String dateTimeStr = data[1].trim();
                String typeStr = data[2].trim();

                Employee emp = company.getEmployees().stream()
                        .filter(e -> e.getId() != null && e.getId().toString().equals(empId))
                        .findFirst()
                        .orElse(null);

                if (emp == null) {
                    System.out.println("LOG : Employé avec l'ID " + empId + " introuvable. Ligne ignorée.");
                    continue;
                }

                AttendanceRecord record = new AttendanceRecord();
                record.setEmployee(emp);
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        .toFormatter();

                record.setTime(LocalDateTime.parse(dateTimeStr, formatter));
                record.setCheckIn(typeStr.equalsIgnoreCase("IN"));

                evaluateAndApplyAttendance(record);
                company.getAttendanceRecords().add(record);
            }

            for (Employee employee : company.getEmployees()) {
                List<AttendanceRecord> employeeHistory = company.getAttendanceRecords().stream()
                        .filter(r -> r.getEmployee() != null && r.getEmployee().getId().equals(employee.getId()))
                        .collect(Collectors.toList());

                recalculateBalance(employee, employeeHistory);
            }

            saveData();
        }
    }

    /**
     * @brief Déclenche la sérialisation et l'écriture de l'état de l'entreprise sur le disque local.
     */
    private void saveData() {
        try {
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }
}