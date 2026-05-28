package com.example.mainapp.service;

import com.example.dto.CheckPoint;
import com.example.mainapp.model.Company;
import com.example.mainapp.network.TCPServer;
// Si tu as un PersistenceManager, n'oublie pas de l'importer !
// import com.example.mainapp.service.PersistenceManager;

import java.time.LocalDateTime;
import java.util.List;

public class PointageService {

    // 1. Instance unique (Singleton)
    private static PointageService instance;

    private final Company company;

    // 2. Constructeur privé
    private PointageService() {
        this.company = TCPServer.getInstance().getCompany();
    }

    // 3. Récupération de l'instance
    public static PointageService getInstance() {
        if (instance == null) {
            instance = new PointageService();
        }
        return instance;
    }

    // ==========================================
    // 🟩 CREATE (Ajouter)
    // ==========================================
    public void ajouterPointage(CheckPoint nouveauPointage) throws Exception {
        if (nouveauPointage == null || nouveauPointage.getEmployeeId() == null) {
            throw new Exception("Le pointage est invalide ou l'employé n'est pas spécifié.");
        }

        // Règle métier (Optionnelle) : On ne peut pas pointer dans le futur
        if (nouveauPointage.getTime() != null && nouveauPointage.getTime().isAfter(LocalDateTime.now())) {
            throw new Exception("Date de pointage invalide : on ne peut pas pointer dans le futur !");
        }

        company.getCheckPoints().add(nouveauPointage);
        sauvegarderDonnees();
    }

    // ==========================================
    // 🟦 READ (Lire)
    // ==========================================
    public List<CheckPoint> recupererTousLesPointages() {
        return company.getCheckPoints();
    }

    // ==========================================
    // 🟧 UPDATE (Mettre à jour)
    // ==========================================
    public void mettreAJourPointage(CheckPoint pointageModifie) throws Exception {
        if (pointageModifie == null) {
            throw new Exception("Le pointage à modifier est invalide.");
        }
        // En mémoire, l'objet est déjà modifié par l'interface, on déclenche la sauvegarde
        sauvegarderDonnees();
    }

    // ==========================================
    // 🟥 DELETE (Supprimer)
    // ==========================================
    public void supprimerPointage(CheckPoint pointageASupprimer) throws Exception {
        if (pointageASupprimer == null) {
            throw new Exception("Impossible de supprimer un pointage nul.");
        }

        boolean supprime = company.getCheckPoints().remove(pointageASupprimer);
        if (!supprime) {
            throw new Exception("Ce pointage n'existe pas ou a déjà été supprimé.");
        }

        sauvegarderDonnees();
    }

    // ==========================================
    // 💾 PERSISTANCE (Sauvegarde)
    // ==========================================
    private void sauvegarderDonnees() {
        try {
            // Décommente cette ligne si ton PersistenceManager gère bien la sauvegarde
            // PersistenceManager.saveData(this.company);
            System.out.println("LOG : Base de données de l'entreprise (Pointages) mise à jour sur le disque.");
        } catch (Exception e) {
            System.err.println("Erreur critique lors de la sauvegarde : " + e.getMessage());
        }
    }
}