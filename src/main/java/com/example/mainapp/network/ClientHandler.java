package com.example.mainapp.network;

import com.example.mainapp.controller.MainController;
import com.example.mainapp.model.company.Company;
import com.example.mainapp.model.employee.Employee;
import com.example.mainapp.model.attendance.AttendanceRecord;
import com.example.dto.EmployeeDTO;
import com.example.dto.CheckPointDTO;
import com.example.mainapp.controller.attendance.AttendanceService;
import com.example.mainapp.utils.PersistenceManager;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire de communication dédié au traitement d'un client réseau (pointeuse).
 * <p>
 * Cette classe implémente {@link Runnable} afin de s'exécuter dans un thread isolé.
 * Elle se charge de lire les objets sérialisés envoyés par l'émulateur de pointeuse
 * et d'y répondre. Elle gère deux cas d'usage principaux : la fourniture de la liste
 * des employés (pour initialiser l'interface de la pointeuse) et le traitement des
 * nouveaux pointages entrants.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class ClientHandler implements Runnable {

    /** Le socket réseau représentant la connexion active avec le client. */
    private final Socket clientSocket;

    /** L'instance centrale de l'entreprise contenant les données métier et les employés. */
    private final Company company;

    /**
     * Construit un nouveau gestionnaire pour traiter les requêtes d'une connexion entrante.
     *
     * @param clientSocket Le socket TCP établi avec la pointeuse cliente.
     * @param company      Le modèle de données central de l'application.
     */
    public ClientHandler(Socket clientSocket, Company company) {
        this.clientSocket = clientSocket;
        this.company = company;
    }

    /**
     * Point d'entrée du thread gérant la communication avec la pointeuse.
     * <p>
     * La méthode ouvre les flux d'entrée et de sortie d'objets, puis écoute la requête du client :
     * <ul>
     * <li>Si la requête est {@code "GET_EMPLOYEES"}, le serveur renvoie une liste de {@link EmployeeDTO}.</li>
     * <li>Si la requête est un objet {@link CheckPointDTO}, le serveur le convertit en {@link AttendanceRecord},
     * vérifie la validité de l'employé, détecte les éventuels doubles pointages (doublons),
     * enregistre la donnée via le {@link AttendanceService}, et actualise l'interface graphique en temps réel.</li>
     * </ul>
     * La connexion (socket) est systématiquement fermée à la fin du traitement, garantissant
     * la libération des ressources réseau.
     * </p>
     */
    @Override
    public void run() {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            Object input = ois.readObject();

            if (input instanceof String && ((String) input).equals("GET_EMPLOYEES")) {
                System.out.println("Requête 'GET_EMPLOYEES' reçue d'un client.");
                List<EmployeeDTO> listeDTO = new ArrayList<>();
                for (Employee emp : company.getEmployees()) {
                    String nomComplet = emp.getName() + " " + emp.getSurname();
                    listeDTO.add(new EmployeeDTO(emp.getId(), nomComplet));
                }
                oos.writeObject(listeDTO);
                oos.flush();
                System.out.println("Liste de " + listeDTO.size() + " DTOs envoyée au client.");
            }


            // Dans ClientHandler.java (Section de traitement du CheckPointDTO)

            else if (input instanceof CheckPointDTO) {
                CheckPointDTO cp = (CheckPointDTO) input;
                String type = cp.isCheckIn() ? "Entrée" : "Sortie";
                System.out.println("POINTAGE DTO REÇU : " + type + " pour ID " + cp.getEmployeeId());

                Employee emp = company.findEmployeeById(cp.getEmployeeId());

                if (emp == null) {
                    System.err.println("Erreur : Pointage refusé, employé introuvable !");
                    oos.writeObject("ERROR: Employé inconnu");
                    oos.flush();
                } else {
                    try {
                        AttendanceRecord record = new AttendanceRecord(emp, cp.getTime(), cp.isCheckIn());
                        AttendanceService.getInstance().addAttendanceRecord(record);
                        oos.writeObject("OK");
                    } catch (Exception e) {
                        System.err.println("Erreur lors du traitement métier : " + e.getMessage());
                        oos.writeObject("ERROR");
                    }

                    oos.flush();

                    // 4. Rafraîchissement UI
                    try {
                        Platform.runLater(() -> {
                            if (MainController.instance != null) {
                                MainController.instance.rafraichirUI();
                            }
                        });
                    } catch (IllegalStateException e) {
                        System.out.println("Application en cours d'arrêt, actualisation ignorée.");
                    }
                }

            }

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement avec un client : " + e.getMessage());
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