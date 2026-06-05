package com.example.mainapp.utils;

import com.example.mainapp.model.company.Company;

import java.io.*;

/**
 * Gestionnaire de persistance chargé de la sauvegarde et du chargement des données de l'application.
 * <p>
 * Cette classe utilitaire fournit des méthodes statiques permettant d'assurer la persistance de l'objet
 * central {@link Company}. Elle utilise le mécanisme de sérialisation native de Java pour convertir
 * l'arborescence complète des objets métier (employés, départements, pointages) en un fichier binaire unique
 * ({@code company_data.ser}). Elle intègre une gestion des erreurs résiliente qui assure la continuité
 * du service en générant des structures de secours en cas d'absence ou de corruption du fichier de sauvegarde.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class PersistenceManager {

    /** Le nom du fichier de sérialisation binaire stocké à la racine de l'application. */
    private static final String DATA_FILE = "company_data.ser";

    /**
     * Sérialise l'objet Company global et l'écrit de manière synchrone sur le disque local.
     * <p>
     * Ouvre un flux de sortie d'objets ({@link ObjectOutputStream} imbriqué dans un {@link FileOutputStream}).
     * Cette méthode convertit l'intégralité du graphe d'objets de l'entreprise en octets binaires.
     * Les exceptions de type {@link IOException} sont interceptées en interne afin de consigner l'erreur
     * dans le canal de diagnostic d'erreur standard sans bloquer le thread utilisateur ou l'interface JavaFX.
     * </p>
     *
     * @param company L'instance centrale de l'objet {@link Company} contenant toutes les données métier à sauvegarder.
     */
    public static void saveData(Company company) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(company);
            System.out.println("Données de l'entreprise sauvegardées avec succès.");
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Lit le fichier binaire local et reconstruit l'arborescence complète de l'objet Company par désérialisation.
     * <p>
     * La méthode effectue un contrôle préalable de l'existence du fichier sur le disque. Si aucun fichier n'est détecté,
     * elle instancie et retourne une entreprise vierge d'initialisation. Si le fichier est présent, elle tente de le restaurer
     * via un {@link ObjectInputStream}. En cas d'échec de lecture (fichier corrompu, version de classe désynchronisée ou
     * {@link ClassNotFoundException}), le gestionnaire intercepte l'anomalie et retourne une instance sécurisée de secours
     * étiquetée "(Recovery)" afin d'éviter un crash critique au démarrage du programme.
     * </p>
     *
     * @return L'objet {@link Company} entièrement restauré avec ses données historiques, ou une instance de repli sécurisée.
     */
    public static Company loadData() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            System.out.println("Aucun fichier de sauvegarde trouvé. Création d'une entreprise vierge.");
            return new Company("Polytech Tours Pointeuse");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Company loadedCompany = (Company) ois.readObject();
            System.out.println("Données chargées avec succès.");
            return loadedCompany;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            return new Company("Polytech Tours Pointeuse (Recovery)");
        }
    }
}