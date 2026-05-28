package com.example.mainapp.service;

import com.example.mainapp.model.AttendanceRecord;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;

import java.time.LocalDateTime;
import java.util.List;

public class AttendanceService {

    // 1. Instance unique (Singleton)
    private static AttendanceService instance;

    private final Company company;

    // 2. Constructeur privé
    private AttendanceService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    // 3. Récupération de l'instance
    public static AttendanceService getInstance() {
        if (instance == null) {
            instance = new AttendanceService();
        }
        return instance;
    }

    // ==========================================
    // 🟩 CREATE (Ajouter)
    // ==========================================
    public void addAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null || record.getEmployee() == null) {
            throw new Exception("Le pointage est invalide ou l'employé n'est pas spécifié.");
        }

        // Règle métier : On ne peut pas pointer dans le futur
        if (record.getTime() != null && record.getTime().isAfter(LocalDateTime.now())) {
            throw new Exception("Date de pointage invalide : on ne peut pas pointer dans le futur !");
        }

        company.getAttendanceRecords().add(record);
        saveData();
    }

    // ==========================================
    // 🟦 READ (Lire)
    // ==========================================
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return company.getAttendanceRecords();
    }

    // ==========================================
    // 🟧 UPDATE (Mettre à jour)
    // ==========================================
    public void updateAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null) {
            throw new Exception("Le pointage à modifier est invalide.");
        }
        // En mémoire, l'objet est déjà modifié par l'interface, on déclenche la sauvegarde
        saveData();
    }

    // ==========================================
    // 🟥 DELETE (Supprimer)
    // ==========================================
    public void deleteAttendanceRecord(AttendanceRecord record) throws Exception {
        if (record == null) {
            throw new Exception("Impossible de supprimer un pointage nul.");
        }

        boolean removed = company.getAttendanceRecords().remove(record);
        if (!removed) {
            throw new Exception("Ce pointage n'existe pas ou a déjà été supprimé.");
        }

        saveData();
    }

    // ==========================================
    // 💾 PERSISTANCE (Sauvegarde)
    // ==========================================
    private void saveData() {
        try {
            PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données de l'entreprise (Pointages) mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur critique lors de la sauvegarde : " + e.getMessage());
        }
    }
}