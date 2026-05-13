package com.example.mainapp.network;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable {

    // 1. L'instance statique privée (Le cœur du Singleton)
    private static TCPServer instance;

    // Variables du serveur
    private int port = 8080; // Port par défaut
    private boolean isRunning = false;
    private ServerSocket serverSocket;

    // 2. Le constructeur PRIVÉ (Empêche de faire 'new TCPServer()')
    private TCPServer() {
        // Initialisation si nécessaire
    }

    // 3. La méthode d'accès globale
    // Le mot-clé 'synchronized' évite les bugs si deux endroits demandent l'instance en même temps
    public static synchronized TCPServer getInstance() {
        if (instance == null) {
            instance = new TCPServer();
        }
        return instance;
    }

    // --- Méthodes de contrôle du serveur ---

    public void demarrer(int port) {
        if (!isRunning) {
            this.port = port;
            // On lance le serveur dans un Thread séparé pour ne pas bloquer JavaFX
            Thread threadServeur = new Thread(this);
            threadServeur.setDaemon(true); // Le thread s'arrêtera si on ferme l'application
            threadServeur.start();
        }
    }

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

    // --- La boucle d'écoute (Méthode exigée par Runnable) ---

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("✅ Serveur TCP démarré et en écoute sur le port " + port);

            while (isRunning) {
                // Le code se bloque ici et attend patiemment qu'une pointeuse se connecte
                Socket clientSocket = serverSocket.accept();
                System.out.println("📡 Nouvelle connexion reçue depuis : " + clientSocket.getInetAddress());

                // TODO: Passer ce 'clientSocket' à un "ClientHandler" pour lire l'objet Pointage
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("❌ Erreur du serveur TCP : " + e.getMessage());
            } else {
                System.out.println("ℹ️ Serveur TCP arrêté proprement.");
            }
        }
    }
}