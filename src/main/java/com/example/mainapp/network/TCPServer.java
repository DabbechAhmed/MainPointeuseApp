package com.example.mainapp.network;

import com.example.mainapp.model.company.Company;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serveur TCP centralisé chargé d'écouter et de gérer les connexions des terminaux de pointage clients.
 * <p>
 * Cette classe implémente le pattern Singleton pour garantir l'unicité du point d'écoute réseau.
 * Elle s'exécute de manière asynchrone dans un thread dédié (via l'interface {@link Runnable})
 * afin de ne pas bloquer le thread principal de l'interface graphique JavaFX.
 * </p>
 * <p>
 * <b>Architecture Concurrente et Multithread :</b>
 * Pour absorber les flux simultanés de plusieurs pointeuses sans perte de données ni goulot d'étranglement,
 * le serveur applique un principe de délégation stricte et non-bloquante :
 * </p>
 * <ul>
 * <li>La boucle principale écoute les connexions entrantes via la méthode {@code accept()}.</li>
 * <li>Dès qu'un signal est capté, le serveur instancie immédiatement un travailleur dédié ({@link ClientHandler}).</li>
 * <li>Ce traitement est encapsulé et lancé dans un nouveau {@link Thread} indépendant.</li>
 * <li>La méthode {@code accept()} est ainsi instantanément libérée et redevient disponible pour intercepter les requêtes concurrentes d'autres terminaux.</li>
 * </ul>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class TCPServer implements Runnable {

    /** Instance unique du serveur TCP (Pattern Singleton). */
    private static TCPServer instance;

    /** Port réseau d'écoute de l'application serveur. */
    private int port = 8080;

    /** Indicateur de cycle de vie du serveur réseau. */
    private boolean isRunning = false;

    /** Socket de service chargé de l'ouverture du canal réseau et de l'écoute. */
    private ServerSocket serverSocket;

    /** Référence vers le modèle de données central de l'entreprise. */
    private Company company;

    /**
     * Constructeur privé restreignant l'instanciation directe pour préserver le pattern Singleton.
     */
    private TCPServer() {
    }

    /**
     * Récupère l'instance du modèle de données de l'entreprise associée au serveur.
     *
     * @return L'instance globale de {@link Company}.
     */
    public Company getCompany() {
        return this.company;
    }

    /**
     * Fournit l'accès à l'instance unique du serveur TCP.
     * <p>
     * La méthode est synchronisée pour sécuriser l'initialisation initiale du Singleton
     * en contexte d'accès concurrent multithread.
     * </p>
     *
     * @return L'instance unique de {@link TCPServer}.
     */
    public static synchronized TCPServer getInstance() {
        if (instance == null) {
            instance = new TCPServer();
        }
        return instance;
    }

    /**
     * Initialise les paramètres et amorce l'écoute réseau asynchrone du serveur.
     *
     * @param port    Le port TCP d'écoute cible.
     * @param company Le modèle de données central à lier aux flux entrants.
     */
    public void demarrer(int port, Company company) {
        if (!isRunning) {
            this.port = port;
            this.company = company;

            Thread threadServeur = new Thread(this);
            threadServeur.setDaemon(true);
            threadServeur.start();
        }
    }

    /**
     * Interrompt l'activité du serveur et libère les sockets actifs.
     */
    public void arreter() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture du serveur : " + e.getMessage());
        }
    }

    /**
     * Boucle d'écoute réseau principale s'exécutant en arrière-plan.
     * <p>
     * Intercepte les connexions via un mécanisme bloquant unitaire ({@code accept()})
     * puis bascule immédiatement la communication dans un fil d'exécution parallèle (Thread dédié)
     * pour préserver la réactivité globale face aux requêtes simultanées de plusieurs pointeuses.
     * </p>
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Serveur TCP démarré sur le port " + port);

            while (isRunning) {
                // Attente non-bloquante pour les autres sockets grâce à la délégation immédiate
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion reçue depuis : " + clientSocket.getInetAddress());

                // Instanciation du gestionnaire et délégation immédiate dans un Thread dédié
                ClientHandler handler = new ClientHandler(clientSocket, this.company);
                Thread threadClient = new Thread(handler);
                threadClient.start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Erreur du serveur TCP : " + e.getMessage());
            } else {
                System.out.println("Serveur TCP arrêté proprement.");
            }
        }
    }
}