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
 * Service métier centralisé gérant l'évaluation des pointages, le calcul des soldes et les imports de données.
 * <p>
 * Cette classe implémente le pattern Singleton et centralise le cœur algorithmique de gestion du temps de l'application.
 * Elle remplit trois fonctions critiques :
 * <ul>
 * <li><b>Évaluation unitaire :</b> Analyse de la conformité d'un pointage par rapport au planning de l'employé et application des statuts (Retard, Avance, Heures supplémentaires, Départ anticipé).</li>
 * <li><b>Recalcul des soldes :</b> Analyse chronologique par blocs d'entrées/sorties pour détecter les anomalies (sorties manquantes) et mettre à jour le solde de minutes de l'employé en fonction du seuil de tolérance.</li>
 * <li><b>Importation de masse :</b> Parsing robuste de fichiers CSV pour injecter des historiques de pointages provenant des pointeuses distantes.</li>
 * </ul>
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class AttendanceService {

    /** L'instance unique (Singleton) du service de gestion des pointages. */
    private static AttendanceService instance;

    /** La référence vers l'entité globale de l'entreprise pour manipuler le modèle de données. */
    private final Company company;

    /**
     * Constructeur privé extrayant l'instance de l'entreprise depuis le serveur TCP actif.
     */
    private AttendanceService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    /**
     * Retourne l'instance unique et globale de ce service.
     * <p>
     * Initialise l'instance de manière paresseuse (Lazy Initialization) lors de son tout premier appel.
     * </p>
     *
     * @return L'instance unique de {@link AttendanceService}.
     */
    public static AttendanceService getInstance() {
        if (instance == null) {
            instance = new AttendanceService();
        }
        return instance;
    }

    /**
     * Qualifie et applique le statut de conformité d'un enregistrement de pointage unique.
     * <p>
     * Cette méthode compare l'horodatage effectif du pointage avec les heures théoriques du planning de l'employé
     * pour le jour de la semaine concerné. Elle s'appuie sur le seuil configuré par le {@link ConfigManager} :
     * <ul>
     * <li><b>En Entrée (Check-In) :</b> Génère un incident si le retard excède la tolérance, ou applique le statut "Avance".</li>
     * <li><b>En Sortie (Check-Out) :</b> Valorise des "Heures supp." si l'employé est resté plus longtemps, ou un incident de "Départ anticipé".</li>
     * </ul>
     * </p>
     *
     * @param record L'enregistrement de pointage {@link AttendanceRecord} à analyser et qualifier.
     */
    public void evaluateAndApplyAttendance(AttendanceRecord record) {
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
     * Recalcule l'intégralité du solde de minutes d'un employé à partir de son historique chronologique.
     * <p>
     * L'algorithme trie d'abord l'historique par date et heure, puis regroupe les événements par journée (LocalDate).
     * Pour chaque jour, il associe les paires d'entrées et de sorties consécutives pour mesurer le temps de présence effectif.
     * <ul>
     * <li><b>Gestion des anomalies :</b> Si une entrée n'a pas de sortie associée et que la journée est passée, une pénalité
     * équivalente au temps de travail théorique attendu est appliquée (retenue totale) et le statut passe en "Sortie manquante".</li>
     * <li><b>Journée en cours :</b> Si l'absence de sortie concerne la journée d'aujourd'hui, l'algorithme considère que la
     * session est en cours et ne pénalise pas le solde.</li>
     * </ul>
     * Enfin, l'écart global (Minutes travaillées - Minutes théoriques dues) est soumis au filtre de tolérance avant d'être affecté à l'employé.
     * </p>
     *
     * @param employee          L'employé {@link Employee} dont le solde doit être mis à jour.
     * @param attendanceHistory La liste complète de tous les pointages historiques rattachés à cet employé.
     */
    public void recalculateBalance(Employee employee, List<AttendanceRecord> attendanceHistory) {
        employee.setSoldeMinutes(0L);

        if (attendanceHistory == null || attendanceHistory.isEmpty()) {
            return;
        }

        attendanceHistory.sort(Comparator.comparing(AttendanceRecord::getTime));

        Map<LocalDate, List<AttendanceRecord>> recordsByDay = attendanceHistory.stream()
                .collect(Collectors.groupingBy(r -> r.getTime().toLocalDate()));

        long newBalance = 0L;
        ConfigManager config = new ConfigManager();
        int tolerance = config.getToleranceMinutes();

        LocalDate today = LocalDate.now();

        for (Map.Entry<LocalDate, List<AttendanceRecord>> entry : recordsByDay.entrySet()) {
            LocalDate date = entry.getKey();
            List<AttendanceRecord> dailyRecords = entry.getValue();

            long workedMinutesToday = 0L;
            boolean missingCheckoutAnomaly = false;
            boolean shiftInProgress = false;

            for (int i = 0; i < dailyRecords.size(); i++) {
                AttendanceRecord current = dailyRecords.get(i);

                if (current.isCheckIn()) {
                    if (i + 1 < dailyRecords.size() && !dailyRecords.get(i + 1).isCheckIn()) {
                        AttendanceRecord checkOut = dailyRecords.get(i + 1);
                        workedMinutesToday += Duration.between(current.getTime(), checkOut.getTime()).toMinutes();

                        if (current.getStatus() != null && current.getStatus().contains("Sortie manquante")) {
                            evaluateAndApplyAttendance(current);
                        }

                        i++;
                    } else {
                        if (date.isBefore(today)) {
                            missingCheckoutAnomaly = true;
                            current.setStatus("Incident : Sortie manquante");
                        } else {
                            shiftInProgress = true;
                        }
                    }
                }
            }

            long expectedMinutes = 0L;
            if (employee.getSchedule() != null) {
                expectedMinutes = employee.getSchedule().getMinutesPourCeJour(date.getDayOfWeek());
            }

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

        employee.setSoldeMinutes(newBalance);
    }

    /**
     * Ajoute un nouveau pointage au modèle de données de l'entreprise et recalcule le solde de l'employé associé.
     * <p>
     * Évalue les critères de conformité temporelle immédiatement, insère l'enregistrement dans la liste centrale,
     * puis isole l'historique complet de l'employé concerné pour lancer un recalcul synchrone du solde de minutes.
     * </p>
     *
     * @param record Le nouvel enregistrement de pointage à valider et insérer.
     * @throws Exception Si l'objet pointage ou sa référence d'employé associée est invalide (null).
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
     * Traite et réévalue un pointage existant suite à une modification ou correction manuelle.
     * <p>
     * Relance l'évaluation des règles de planning, ajoute la mention textuelle "(Modifié)" au statut de
     * l'enregistrement, puis met à jour le solde d'heures de l'employé en recalculant son historique.
     * </p>
     *
     * @param record L'enregistrement de pointage modifié à régulariser.
     * @throws Exception Si la référence du pointage est nulle.
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
     * Récupère la liste brute de tous les enregistrements de pointage de l'entreprise.
     *
     * @return Une {@link List} contenant l'ensemble des objets {@link AttendanceRecord}.
     */
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return company.getAttendanceRecords();
    }

    /**
     * Supprime définitivement un enregistrement de pointage du modèle central de données.
     * <p>
     * Supprime l'objet de la collection globale et déclenche instantanément une régularisation du solde de minutes
     * de l'employé impacté pour effacer l'empreinte de ce pointage de son historique de présence.
     * </p>
     *
     * @param record L'enregistrement de pointage à radier de l'application.
     * @throws Exception Si le pointage est nul ou introuvable dans le système.
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
     * Importe de manière massive un flux de pointages à partir d'un fichier CSV externe.
     * <p>
     * Cette méthode procède par étapes séquentielles :
     * <ul>
     * <li>Ouvre un canal de lecture tamponné (BufferedReader) et saute la ligne d'en-tête du fichier.</li>
     * <li>Parse chaque ligne textuelle autour du séparateur {@code ";"}.</li>
     * <li>Recherche l'employé correspondant à l'identifiant (UUID) lu à l'aide d'un filtre Stream. Si l'employé n'existe pas, la ligne est ignorée.</li>
     * <li>Instancie un {@link AttendanceRecord} et interprète l'horodatage via un parseur flexible multiformat (acceptant avec ou sans deux-points).</li>
     * <li>Qualifie le type de signalement (IN/OUT), applique l'évaluation métier unitaire et injecte la donnée dans le modèle central.</li>
     * </ul>
     * Une fois la lecture complète achevée, elle exécute une boucle finale pour recalculer de bout en bout les compteurs de soldes
     * de chaque employé présent dans l'organisation, puis force l'écriture physique sur disque.
     * </p>
     *
     * @param file Le descripteur de fichier {@link File} représentant le CSV sélectionné.
     * @throws Exception Si le fichier est nul, introuvable ou si la structure d'une colonne viole les contraintes de formatage.
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
     * Déclenche la sérialisation et l'écriture de l'état de l'entreprise sur le disque local.
     * <p>
     * Méthode interne de synchronisation déléguant la persistance binaire au {@link PersistenceManager}.
     * </p>
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