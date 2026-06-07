package com.example.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Objet de transfert de données (DTO) modélisannt un événement de pointage brut circulant sur le réseau.
 * <p>
 * Classe immuable faisant office de conteneur léger (payload) envoyé par les terminaux
 * clients vers le serveur TCP central.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class CheckPointDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID employeeId;
    private final LocalDateTime time;
    private final boolean isCheckIn;

    /**
     * Construit un nouveau vecteur de transfert de pointage réseau.
     *
     * @param employeeId L'identifiant unique de l'employé émetteur.
     * @param time       L'horodatage de l'événement.
     * @param isCheckIn  Le sens du flux (vrai pour une entrée, faux pour une sortie).
     */
    public CheckPointDTO(UUID employeeId, LocalDateTime time, boolean isCheckIn) {
        this.employeeId = employeeId;
        this.time = time;
        this.isCheckIn = isCheckIn;
    }

    public UUID getEmployeeId() { return employeeId; }
    public LocalDateTime getTime() { return time; }
    public boolean isCheckIn() { return isCheckIn; }
}