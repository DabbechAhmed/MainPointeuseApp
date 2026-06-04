package com.example.mainapp.model.attendance;

import com.example.mainapp.model.employee.Employee;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente un enregistrement de pointage (entrée ou sortie) d'un employé au sein du système.
 * <p>
 * Cette classe encapsule toutes les informations associées à un événement de pointage :
 * l'employé concerné, l'horodatage exact de l'action, la nature du pointage (arrivée ou départ)
 * et un indicateur de statut permettant de suivre la conformité par rapport au planning
 * (Normal, Retard, Doublon, Sortie manquante, etc.).
 * Elle implémente {@link Serializable} pour permettre sa persistance sur le disque. Chaque pointage
 * possède son propre identifiant unique sous la forme d'un {@link UUID}.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class AttendanceRecord implements Serializable {

    @Serial
    /** Identifiant de structure pour la sérialisation et la désérialisation de la classe. */
    private static final long serialVersionUID = 1L;

    /** Identifiant unique et immuable du pointage. */
    private final UUID  id;

    /** L'employé ayant réalisé ce pointage. */
    private Employee employee;

    /** La date et l'heure exactes de l'enregistrement du pointage. */
    private LocalDateTime time;

    /** Indicateur du type d'action (vrai s'il s'agit d'une entrée/Check-In, faux pour une sortie/Check-Out). */
    private boolean isCheckIn;

    /** Le libellé qualifiant l'état ou l'anomalie du pointage (ex: "Normal", "Incident : Retard"). */
    private String status;

    /**
     * Construit un enregistrement de pointage complet et initialise automatiquement un identifiant unique.
     * <p>
     * Par défaut, le statut d'un nouveau pointage est défini sur {@code "Normal"} avant son évaluation
     * par les services métier de l'application centrale.
     * </p>
     *
     * @param employee  L'employé associé au pointage.
     * @param time      L'horodatage précis (date et heure) de l'événement.
     * @param isCheckIn {@code true} s'il s'agit d'une entrée, {@code false} s'il s'agit d'une sortie.
     */
    public AttendanceRecord(Employee employee, LocalDateTime time, boolean isCheckIn) {
        this.id = UUID.randomUUID();
        this.employee = employee;
        this.time = time;
        this.isCheckIn = isCheckIn;
        this.status = "Normal";
    }

    /**
     * Constructeur par défaut initialisant un pointage vierge doté d'un identifiant unique automatique.
     * <p>
     * Ce constructeur sans argument initialise les références à {@code null} et est principalement
     * exploité par les architectures de sérialisation, de liaison de données (Data Binding) ou JavaFX.
     * </p>
     */
    public AttendanceRecord(){
        this.id = UUID.randomUUID();
        this.employee = null;
        this.time = null;
        this.isCheckIn = true;
        this.status = "Normal";
    }

    /**
     * Récupère l'identifiant unique de cet enregistrement de pointage.
     *
     * @return L'identifiant {@link UUID} du pointage.
     */
    public UUID getId() { return id; }

    /**
     * Récupère l'employé lié à cet enregistrement.
     *
     * @return L'objet {@link Employee} concerné.
     */
    public Employee getEmployee() { return employee; }

    /**
     * Associe ou modifie l'employé lié à cet enregistrement.
     *
     * @param employee Le nouvel employé à assigner au pointage.
     */
    public void setEmployee(Employee employee) { this.employee = employee; }

    /**
     * Récupère la date et l'heure du pointage.
     *
     * @return Un objet {@link LocalDateTime} représentant l'instant du pointage.
     */
    public LocalDateTime getTime() { return time; }

    /**
     * Modifie l'horodatage associé au pointage.
     *
     * @param time La nouvelle date et heure à appliquer.
     */
    public void setTime(LocalDateTime time) { this.time = time; }

    /**
     * Indique si l'action courante correspond à une entrée dans l'entreprise.
     *
     * @return {@code true} si c'est un signalement d'arrivée (Check-In), {@code false} si c'est un départ (Check-Out).
     */
    public boolean isCheckIn() { return isCheckIn; }

    /**
     * Définit la nature du pointage (entrée ou sortie).
     *
     * @param checkIn {@code true} pour configurer une entrée, {@code false} pour configurer une sortie.
     */
    public void setCheckIn(boolean checkIn) { isCheckIn = checkIn; }

    /**
     * Récupère le statut textuel actuel de conformité de ce pointage.
     *
     * @return La chaîne de caractères décrivant l'état ou l'incident de l'enregistrement.
     */
    public String getStatus() { return status; }

    /**
     * Met à jour le statut textuel ou l'anomalie après évaluation ou correction manuelle.
     *
     * @param status Le nouveau message de statut à attribuer (ex: "Incident : Sortie manquante").
     */
    public void setStatus(String status) { this.status = status; }
}