package com.example.mainapp.network;

import com.example.mainapp.model.Company; // ✅ Nouvel import
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable {

    private static TCPServer instance;

    private int port = 8080;
    private boolean isRunning = false;
    private ServerSocket serverSocket;
    private Company company;

    private TCPServer() {
    }

    public Company getCompany() {
        return this.company;
    }

    public static synchronized TCPServer getInstance() {
        if (instance == null) {
            instance = new TCPServer();
        }
        return instance;
    }

    public void demarrer(int port, Company company) {
        if (!isRunning) {
            this.port = port;
            this.company = company; // Sauvegarde du pointeur de données

            Thread threadServeur = new Thread(this);
            threadServeur.setDaemon(true);
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

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("✅ Serveur TCP démarré et en écoute sur le port " + port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("📡 Nouvelle connexion reçue depuis : " + clientSocket.getInetAddress());

                // ✅ Remplacement du TODO par la création et le lancement du ClientHandler
                ClientHandler handler = new ClientHandler(clientSocket, this.company);
                Thread threadClient = new Thread(handler);
                threadClient.start(); // S'exécute en arrière-plan pour ce client précis
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