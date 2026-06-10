package com.example.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Objet de transfert de données (DTO) modélisant un événement de pointage brut circulant sur le réseau.
 * <p>
 * Cette classe a été conçue comme un conteneur de données léger, thread-safe et strictement **immuable** * (tous ses champs sont déclarés {@code final}). Elle sert de vecteur de communication (payload)
 * envoyé de manière synchrone par l'application cliente (émulateur de pointeuse) ou extrait lors d'une
 * synchronisation asynchrone depuis le stockage local hors-ligne vers le serveur TCP central.
 * </p>
 * <p>
 * Elle encapsule uniquement les primitives temporelles et d'identification nécessaires et suffisantes
 * à la caractérisation d'un événement de présence, laissant la responsabilité de la qualification
 * métier (statuts, doublons, calculs de soldes) à la couche de service du serveur central.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class CheckPointDTO implements Serializable {

    /** Numéro de version unique utilisé lors de la désérialisation pour garantir la compatibilité binaire entre le client et le serveur. */
    private static final long serialVersionUID = 1L;

    /** Identifiant unique (UUID) de l'employé associé à l'interaction sur le terminal physique. */
    private final UUID employeeId;

    /** Marqueur temporel précis contenant la date et l'heure de l'événement (généralement arrondi au quart d'heure supérieur ou inférieur). */
    private final LocalDateTime time;

    /** Indicateur logique du sens de flux de présence : {@code true} pour une Entrée (Check-In), {@code false} pour une Sortie (Check-Out). */
    private final boolean isCheckIn;

    /**
     * Construit un nouveau vecteur de transfert de pointage réseau immuable.
     * <p>
     * Ce constructeur fige l'état du DTO au moment de l'action de pointage sur l'interface graphique client.
     * </p>
     *
     * @param employeeId L'identifiant unique {@link UUID} de l'employé émetteur.
     * @param time       L'horodatage {@link LocalDateTime} validé et arrondi de l'événement.
     * @param isCheckIn  Le sens du flux (vrai pour une entrée, faux pour une sortie).
     */
    public CheckPointDTO(UUID employeeId, LocalDateTime time, boolean isCheckIn) {
        this.employeeId = employeeId;
        this.time = time;
        this.isCheckIn = isCheckIn;
    }

    /**
     * Récupère l'identifiant unique de l'employé à l'origine du signal réseau.
     *
     * @return L'{@link UUID} de l'employé concerné.
     */
    public UUID getEmployeeId() {
        return employeeId;
    }

    /**
     * Récupère l'horodatage de l'événement transporté par ce DTO.
     *
     * @return L'objet {@link LocalDateTime} représentant la date et l'heure du pointage.
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * Indique la nature de l'enregistrement de présence (Entrée ou Sortie).
     *
     * @return {@code true} s'il s'agit d'une action d'entrée (Check-In), {@code false} s'il s'agit d'une sortie (Check-Out).
     */
    public boolean isCheckIn() {
        return isCheckIn;
    }
}