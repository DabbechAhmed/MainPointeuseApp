package com.example.mainapp.network;

import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.dto.EmployeeDTO; // ✅ Import du DTO

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

            // 1. Traiter la demande de récupération des employés
            if (input instanceof String && ((String) input).equals("GET_EMPLOYEES")) {
                System.out.println("📥 Requête 'GET_EMPLOYEES' reçue d'un client.");

                // ✅ Création de la liste allégée (DTO)
                List<EmployeeDTO> listeDTO = new ArrayList<>();

                for (Employee emp : company.getEmployees()) {
                    String nomComplet = emp.getName() + " " + emp.getSurname();
                    listeDTO.add(new EmployeeDTO(emp.getId(), nomComplet));
                }

                // Envoi exclusif des DTOs sur le réseau
                oos.writeObject(listeDTO);
                oos.flush();
                System.out.println("📤 Liste de " + listeDTO.size() + " DTOs envoyée au client.");
            }

            // 2. Traiter la réception d'un pointage (CheckPoint) plus tard
            /* else if (input instanceof CheckPoint) {
                CheckPoint cp = (CheckPoint) input;
                // company.ajouterPointage(cp.getEmployeeId(), cp.getTime());
            } */

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