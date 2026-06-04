package com.example.mainapp.network;

import com.example.mainapp.model.company.Company;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serveur TCP chargé d'écouter et d'accepter les connexions des pointeuses clientes.
 * <p>
 * Cette classe implémente le pattern Singleton pour garantir une instance unique
 * du serveur au sein de l'application centrale. Elle s'exécute dans un thread dédié
 * (via {@link Runnable}) et délègue le traitement de chaque connexion entrante à un
 * {@link ClientHandler} s'exécutant dans son propre thread. Cela permet au système
 * de gérer plusieurs pointeuses simultanément sans se bloquer.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class TCPServer implements Runnable {

    /** L'instance unique (Singleton) du serveur TCP. */
    private static TCPServer instance;

    /** Le port réseau d'écoute du serveur (par défaut 8080). */
    private int port = 8080;

    /** Indicateur d'état du serveur (vrai si le serveur est en cours d'exécution). */
    private boolean isRunning = false;

    /** Le socket serveur gérant la réception des connexions réseau entrantes. */
    private ServerSocket serverSocket;

    /** L'instance centrale de l'entreprise contenant les données métier (employés, pointages). */
    private Company company;

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * <p>
     * L'accès à l'instance doit se faire exclusivement via la méthode {@link #getInstance()}.
     * </p>
     */
    private TCPServer() {
    }

    /**
     * Récupère l'instance de l'entreprise gérée par le serveur.
     *
     * @return L'objet {@link Company} contenant toutes les données de l'application.
     */
    public Company getCompany() {
        return this.company;
    }

    /**
     * Retourne l'instance unique du serveur TCP.
     * <p>
     * Cette méthode est synchronisée pour garantir la sécurité des threads (thread-safe)
     * lors de la toute première initialisation du Singleton.
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
     * Démarre le serveur TCP sur un port spécifique.
     * <p>
     * Si le serveur n'est pas déjà en cours d'exécution, cette méthode enregistre
     * les paramètres métier et lance le processus d'écoute réseau dans un nouveau thread démon
     * (qui s'arrêtera automatiquement à la fermeture de l'application).
     * </p>
     *
     * @param port    Le port réseau sur lequel le serveur doit écouter les pointeuses.
     * @param company L'objet central de l'entreprise pour le partage et la modification des données.
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
     * Arrête proprement le serveur TCP.
     * <p>
     * Cette méthode modifie l'indicateur d'état et ferme le socket serveur,
     * ce qui interrompt immédiatement la boucle d'attente réseau et libère le port.
     * </p>
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
     * Boucle principale d'exécution du serveur réseau.
     * <p>
     * Ouvre le port réseau et attend indéfiniment de nouvelles connexions entrantes de la part
     * des émulateurs de pointeuses. À chaque nouvelle connexion acceptée, un objet
     * {@link ClientHandler} est instancié et démarré dans un thread séparé.
     * </p>
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Serveur TCP démarré sur le port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouvelle connexion reçue depuis : " + clientSocket.getInetAddress());

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