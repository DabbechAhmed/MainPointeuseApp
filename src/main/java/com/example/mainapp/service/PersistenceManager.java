package com.example.mainapp.service;

import com.example.mainapp.model.Company;

import java.io.*;

public class PersistenceManager {

    // Le nom du fichier binaire qui sera créé à la racine de ton projet
    private static final String DATA_FILE = "company_data.ser";

    /**
     * Transforme l'objet Company en binaire et l'écrit sur le disque.
     */
    public static void saveData(Company company) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(company);
            System.out.println("✅ Données de l'entreprise sauvegardées avec succès.");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Lit le fichier binaire et reconstruit l'objet Company.
     */
    public static Company loadData() {
        File file = new File(DATA_FILE);

        // Si c'est le tout premier lancement, le fichier n'existe pas encore
        if (!file.exists()) {
            System.out.println("ℹ️ Aucun fichier de sauvegarde trouvé. Création d'une entreprise vierge.");
            return new Company("Polytech Tours Pointeuse");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Company loadedCompany = (Company) ois.readObject();
            System.out.println("✅ Données chargées avec succès.");
            return loadedCompany;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Erreur lors du chargement : " + e.getMessage());
            // En cas d'erreur (fichier corrompu), on repart sur une entreprise vierge
            return new Company("Polytech Tours Pointeuse (Recovery)");
        }
    }
}