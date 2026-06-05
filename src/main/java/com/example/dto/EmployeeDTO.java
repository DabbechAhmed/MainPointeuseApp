package com.example.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Objet de transfert de données (DTO) représentant un employé de manière simplifiée pour les échanges réseau.
 * <p>
 * Cette classe immuable est conçue pour optimiser la communication entre l'application centrale et
 * les émulateurs de pointeuses clientes. Elle encapsule uniquement les informations strictement
 * nécessaires à l'identification visuelle et logique de l'employé (son UUID et son nom complet),
 * évitant ainsi la sérialisation lourde et non sécurisée de l'entité métier complète {@code Employee}
 * (qui embarque son planning, son service et son solde d'heures).
 * Elle implémente {@link Serializable} pour permettre son transit à travers les flux TCP.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class EmployeeDTO implements Serializable {

    /** Identifiant de structure pour la sérialisation et la désérialisation de la classe. */
    private static final long serialVersionUID = 1L;

    /** Identifiant unique et immuable de l'employé (UUID). */
    private final UUID id;

    /** Le nom complet agrégé de l'employé (Prénom + Nom de famille). */
    private final String fullName;

    /**
     * Construit un objet de transfert immuable avec les identifiants requis.
     *
     * @param id       L'identifiant unique {@link UUID} de l'employé.
     * @param fullName Le nom complet formaté et prêt pour l'affichage graphique.
     */
    public EmployeeDTO(UUID id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    /**
     * Récupère l'identifiant unique de l'employé.
     *
     * @return L'identifiant {@link UUID} encapsulé.
     */
    public UUID getId() { return id; }

    /**
     * Retourne la représentation textuelle de l'employé pour les composants graphiques.
     * <p>
     * Cette redéfinition est particulièrement exploitée par les listes déroulantes (ComboBox)
     * ou les cellules d'affichage afin de présenter directement le nom complet sans traitement additionnel.
     * </p>
     *
     * @return Le nom complet de l'employé sous forme de chaîne de caractères.
     */
    @Override
    public String toString() {
        return fullName;
    }
}