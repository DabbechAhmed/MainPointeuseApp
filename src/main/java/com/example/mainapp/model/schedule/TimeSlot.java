package com.example.mainapp.model.schedule;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Représente une plage horaire journalière délimitée par une heure d'arrivée et une heure de départ théoriques.
 * <p>
 * Cette classe de domaine permet de modéliser les fenêtres de travail quotidiennes utilisées dans la construction
 * des plannings ({@link Schedule}). Elle intègre des mécanismes de validation défensive rigoureux pour garantir
 * la cohérence temporelle de la plage, interdisant de manière stricte toute configuration où l'heure de départ
 * précéderait ou égalerait l'heure d'arrivée.
 * Elle implémente {@link Serializable} pour permettre sa persistance sur le disque.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class TimeSlot implements Serializable {

    @Serial
    /** Identifiant de structure pour la sérialisation et la désérialisation de la classe. */
    private static final long serialVersionUID = 1L;

    /** L'heure théorique d'arrivée de la plage horaire. */
    private LocalTime arrivee;

    /** L'heure théorique de départ de la plage horaire. */
    private LocalTime depart;

    /**
     * Constructeur par défaut initialisant une plage horaire standard de journée complète.
     * <p>
     * Par défaut, la plage est configurée pour débuter à **08h00** et se terminer à **17h00**.
     * </p>
     */
    public TimeSlot() {
        this.arrivee = LocalTime.of(8, 0);
        this.depart = LocalTime.of(17, 0);
    }

    /**
     * Construit une plage horaire sur mesure à partir des heures d'arrivée et de départ spécifiées.
     * <p>
     * Un contrôle d'intégrité est immédiatement opéré pour s'assurer que la chronologie de la journée est respectée.
     * </p>
     *
     * @param arrivee L'heure de début/arrivée de la plage.
     * @param depart  L'heure de fin/départ de la plage.
     * @throws IllegalArgumentException Si l'heure de départ est antérieure ou égale à l'heure d'arrivée.
     */
    public TimeSlot(LocalTime arrivee, LocalTime depart) {
        if (depart.isBefore(arrivee) || depart.equals(arrivee)) {
            throw new IllegalArgumentException("L'heure de départ doit être après l'heure d'arrivée");
        }
        this.arrivee = arrivee;
        this.depart = depart;
    }

    /**
     * Récupère l'heure d'arrivée de cette plage horaire.
     *
     * @return Un objet {@link LocalTime} représentant l'heure de début.
     */
    public LocalTime getArrivee() {
        return arrivee;
    }

    /**
     * Modifie l'heure d'arrivée de la plage horaire.
     * <p>
     * Cette méthode vérifie de manière sécurisée que la nouvelle heure d'arrivée reste bien
     * positionnée avant l'heure de départ actuelle.
     * </p>
     *
     * @param arrivee La nouvelle heure de début à appliquer.
     * @throws IllegalArgumentException Si la nouvelle heure d'arrivée est postérieure à l'heure de départ.
     */
    public void setArrivee(LocalTime arrivee) {
        if (this.depart != null && this.depart.isBefore(arrivee)) {
            throw new IllegalArgumentException("L'heure d'arrivée doit être avant l'heure de départ");
        }
        this.arrivee = arrivee;
    }

    /**
     * Récupère l'heure de départ de cette plage horaire.
     *
     * @return Un objet {@link LocalTime} représentant l'heure de fin.
     */
    public LocalTime getDepart() {
        return depart;
    }

    /**
     * Modifie l'heure de départ de la plage horaire.
     * <p>
     * Cette méthode vérifie de manière sécurisée que la nouvelle heure de départ reste bien
     * positionnée après l'heure d'arrivée actuelle.
     * </p>
     *
     * @param depart La nouvelle heure de fin à appliquer.
     * @throws IllegalArgumentException Si la nouvelle heure de départ est antérieure à l'heure d'arrivée.
     */
    public void setDepart(LocalTime depart) {
        if (this.arrivee != null && depart.isBefore(this.arrivee)) {
            throw new IllegalArgumentException("L'heure de départ doit être après l'heure d'arrivée");
        }
        this.depart = depart;
    }

    /**
     * Évalue l'égalité logique entre cette plage horaire et un autre objet.
     * <p>
     * L'égalité est validée si et seulement si l'autre objet est une instance de {@code TimeSlot}
     * et possède des heures d'arrivée et de départ strictement identiques.
     * </p>
     *
     * @param o L'objet à comparer avec l'instance courante.
     * @return {@code true} si les plages horaires sont identiques, {@code false} sinon.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(arrivee, timeSlot.arrivee) && Objects.equals(depart, timeSlot.depart);
    }

    /**
     * Calcule le code de hachage unique de l'objet plage horaire.
     * <p>
     * L'empreinte est générée en combinant les hachages des objets {@code arrivee} et {@code depart}
     * pour maintenir la cohérence avec la méthode {@link #equals(Object)}.
     * </p>
     *
     * @return La valeur entière du hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(arrivee, depart);
    }

    /**
     * Génère une représentation textuelle synthétique de la plage horaire.
     * <p>
     * Le format de sortie suit le standard graphique suivant : {@code HH:mm - HH:mm} (ex: {@code 08:00 - 17:00}).
     * </p>
     *
     * @return Une chaîne de caractères formatée représentant la plage.
     */
    @Override
    public String toString() {
        return arrivee + " - " + depart;
    }
}