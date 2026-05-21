package com.example.mainapp.network;

import com.example.mainapp.controller.MainController;
import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.dto.EmployeeDTO;
import com.example.dto.CheckPoint; // ✅ Import du CheckPoint
import com.example.mainapp.service.PersistenceManager; // ✅ Import pour la sauvegarde
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Company company;

    public ClientHandler(Socket clientSocket, Company company) {
        this.clientSocket = clientSocket;
        this.company = company;
    }

    @Override
    public void run() {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            Object input = ois.readObject();

            if (input instanceof String && ((String) input).equals("GET_EMPLOYEES")) {
                System.out.println("📥 Requête 'GET_EMPLOYEES' reçue d'un client.");
                List<EmployeeDTO> listeDTO = new ArrayList<>();
                for (Employee emp : company.getEmployees()) {
                    String nomComplet = emp.getName() + " " + emp.getSurname();
                    listeDTO.add(new EmployeeDTO(emp.getId(), nomComplet));
                }
                oos.writeObject(listeDTO);
                oos.flush();
                System.out.println("📤 Liste de " + listeDTO.size() + " DTOs envoyée au client.");
            }

            // ✅ NOUVEAU : Traiter la réception d'un pointage
            else if (input instanceof CheckPoint) {
                CheckPoint cp = (CheckPoint) input;
                String type = cp.isCheckIn() ? "Entrée" : "Sortie";
                System.out.println("📥 POINTAGE REÇU : " + type + " pour ID " + cp.getEmployeeId());

                // 1. Ajouter le pointage à la mémoire de l'entreprise
                company.addCheckPoint(cp);

                // 2. Sauvegarder immédiatement sur le disque
                PersistenceManager.saveData(company);
                System.out.println("💾 Pointage sauvegardé avec succès.");

                // ✅ 3. Dire à la pointeuse que c'est bon !
                oos.writeObject("OK");
                oos.flush();

                // ✅ 4. MAGIE JAVAFX : Actualisation en temps réel de la table !
                Platform.runLater(() -> {
                    if (MainController.instance != null) {
                        MainController.instance.rafraichirUI();
                        System.out.println("🔄 Interface graphique actualisée automatiquement !");
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement avec un client : " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                System.err.println("Erreur fermeture socket client : " + e.getMessage());
            }
        }
    }
}