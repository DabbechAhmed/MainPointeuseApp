package com.example.mainapp.network;

import com.example.mainapp.controller.MainController;
import com.example.mainapp.model.Company;
import com.example.mainapp.model.Employee;
import com.example.mainapp.model.AttendanceRecord;
import com.example.dto.EmployeeDTO;
import com.example.dto.CheckPoint;
import com.example.mainapp.controller.attendance.AttendanceService; // ✅ NOUVEL IMPORT
import com.example.mainapp.utils.PersistenceManager;
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

            // ✅ RÉCEPTION DU DTO
            else if (input instanceof CheckPoint) {
                CheckPoint cp = (CheckPoint) input;
                String type = cp.isCheckIn() ? "Entrée" : "Sortie";
                System.out.println("📥 POINTAGE DTO REÇU : " + type + " pour ID " + cp.getEmployeeId());

                // 1. 🔄 TRADUCTION : On cherche le vrai employé
                Employee emp = company.findEmployeeById(cp.getEmployeeId());

                if (emp == null) {
                    System.err.println("❌ Erreur : Pointage refusé, employé introuvable !");
                    oos.writeObject("ERROR: Employé inconnu");
                    oos.flush();
                } else {
                    // 2. CRÉATION DU VRAI MODÈLE MÉTIER
                    AttendanceRecord record = new AttendanceRecord(emp, cp.getTime(), cp.isCheckIn());
                    boolean isDoublon = false;

                    // --- 🚀 LOGIQUE F6 (PARTIE 1) : DÉTECTION DES DOUBLONS ---
                    if (company.getAttendanceRecords() != null) {
                        for (AttendanceRecord existant : company.getAttendanceRecords()) {
                            if (existant.getEmployee().getId().equals(record.getEmployee().getId()) &&
                                    existant.isCheckIn() == record.isCheckIn() &&
                                    existant.getTime().toLocalDate().equals(record.getTime().toLocalDate())) {

                                record.setStatus("Incident : Doublon");
                                isDoublon = true;
                                System.out.println("⚠️ ALERTE : Double pointage détecté pour " + emp.getName() + " !");
                                break;
                            }
                        }
                    }

                    // --- 🚀 LOGIQUE F6 (PARTIE 2) : DÉLÉGATION AU SERVICE ---
                    try {
                        if (isDoublon) {
                            // Si c'est un doublon, on l'ajoute juste pour la trace, sans recalculer le solde
                            company.addAttendanceRecord(record);
                            PersistenceManager.saveData(company);
                        } else {
                            // ✅ LE SERVICE FAIT TOUT : Calcule le solde, met à jour le statut, ajoute à la liste et sauvegarde
                            AttendanceService.getInstance().addAttendanceRecord(record);
                        }

                        // 3. Dire à la pointeuse que c'est bon
                        oos.writeObject("OK");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors du traitement métier : " + e.getMessage());
                        oos.writeObject("ERROR");
                    }

                    oos.flush();

                    // 4. MAGIE JAVAFX : Actualisation en temps réel de la table et du Dashboard !
                    try {
                        Platform.runLater(() -> {
                            if (MainController.instance != null) {
                                MainController.instance.rafraichirUI();
                            }
                        });
                    } catch (IllegalStateException e) {
                        // L'application est en cours d'arrêt, ignore cette erreur
                        System.out.println("ℹ️ Application en cours d'arrêt, actualisation ignorée.");
                    }
                }
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