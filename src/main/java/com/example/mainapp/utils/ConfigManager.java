package com.example.mainapp.utils;

import java.io.*;
import java.util.Properties;

/**
 * Gestionnaire de configuration chargé de charger, modifier et sauvegarder les paramètres système de l'application.
 * <p>
 * Cette classe utilitaire s'appuie sur l'API {@link Properties} de Java pour lire et écrire des paires
 * clé-valeur persistées dans un fichier de configuration local ({@code server_config.properties}). Elle centralise
 * l'accès aux variables système critiques, telles que le port d'écoute du serveur TCP et le seuil de tolérance
 * (en minutes) accordé aux pointages des employés. Si le fichier est manquant ou corrompu, des valeurs d'usine
 * s'auto-génèrent pour garantir la résilience de l'application.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class ConfigManager {

    /** Le nom du fichier de configuration plat stocké à la racine de l'application. */
    private static final String CONFIG_FILE = "server_config.properties";

    /** La structure de données contenant l'ensemble des paires de clés et valeurs de configuration. */
    private final Properties properties;

    /**
     * Construit un gestionnaire de configuration et déclenche le chargement des paramètres.
     * <p>
     * Instancie l'objet {@link Properties} avant de tenter de lire le fichier de propriétés
     * à l'aide de la méthode privée {@link #loadConfig()}.
     * </p>
     */
    public ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    /**
     * Charge les paramètres système depuis le fichier de configuration local.
     * <p>
     * Tente d'ouvrir un flux d'entrée sur le fichier {@code server_config.properties}. Si le fichier
     * n'existe pas ou s'il est illisible, la méthode intercepte l'exception, configure des valeurs par défaut
     * sécurisées (Port 8080, Tolérance de 15 minutes) et force la création d'un fichier propre sur le disque
     * via {@link #saveConfig()}.
     * </p>
     */
    private void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            properties.setProperty("server.port", "8080");
            properties.setProperty("tolerance.minutes", "15");
            saveConfig();
        }
    }

    /**
     * Sauvegarde l'état actuel des propriétés en mémoire dans le fichier de configuration local.
     * <p>
     * Ouvre un flux de sortie pour sérialiser textuellement l'ensemble des configurations clés-valeurs
     * dans le fichier {@code server_config.properties}, accompagné d'un commentaire d'en-tête de métadonnées.
     * </p>
     */
    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Server Configuration");
        } catch (IOException io) {
            System.err.println("Erreur de sauvegarde du fichier de configuration : " + io.getMessage());
        }
    }

    /**
     * Récupère le port d'écoute réseau configuré pour le serveur TCP.
     * <p>
     * Extrait la valeur associée à la clé {@code "server.port"}. Si la valeur est manquante ou si sa conversion
     * numérique échoue (format de texte invalide), la méthode retourne de manière sécurisée le port d'usine par défaut (8080).
     * </p>
     *
     * @return Le numéro de port réseau sous forme d'un entier.
     */
    public int getServerPort() {
        try {
            return Integer.parseInt(properties.getProperty("server.port", "8080"));
        } catch (NumberFormatException e) {
            return 8080;
        }
    }

    /**
     * Met à jour la valeur du port du serveur dans l'objet des propriétés en mémoire.
     * <p>
     * Convertit l'entier fourni en chaîne de caractères pour l'assigner à la clé {@code "server.port"}.
     * Cette modification reste volatile et nécessite un appel ultérieur à {@link #saveConfig()} pour être persistée.
     * </p>
     *
     * @param port Le nouveau numéro de port réseau à configurer.
     */
    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }

    /**
     * Récupère le seuil de tolérance temporel accordé aux pointages des employés.
     * <p>
     * Extrait la valeur associée à la clé {@code "tolerance.minutes"}. Cet écart (en minutes) permet aux
     * algorithmes de calcul de solde de ne pas pénaliser de légères variations d'horaires. Si la valeur est corrompue
     * ou absente, la valeur de sécurité par défaut (15 minutes) est renvoyée.
     * </p>
     *
     * @return Le nombre de minutes de tolérance sous forme d'un entier.
     */
    public int getToleranceMinutes() {
        try {
            return Integer.parseInt(properties.getProperty("tolerance.minutes", "15"));
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    /**
     * Met à jour le seuil de tolérance aux pointages dans l'objet des propriétés en mémoire.
     * <p>
     * Convertit l'entier fourni en chaîne de caractères pour l'assigner à la clé {@code "tolerance.minutes"}.
     * Cette modification reste volatile et nécessite un appel ultérieur à {@link #saveConfig()} pour être persistée.
     * </p>
     *
     * @param minutes Le nouveau nombre de minutes de tolérance à configurer.
     */
    public void setToleranceMinutes(int minutes) {
        properties.setProperty("tolerance.minutes", String.valueOf(minutes));
    }
}