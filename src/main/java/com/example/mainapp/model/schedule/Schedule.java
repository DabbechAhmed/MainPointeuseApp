package com.example.mainapp.model.schedule;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Gère le planning de travail hebdomadaire d'un employé en associant chaque jour de la semaine à une plage horaire.
 * <p>
 * Cette classe s'appuie sur une structure {@link EnumMap} hautement optimisée pour lier chaque élément de
 * l'énumération {@link DayOfWeek} à un objet {@link TimeSlot}. Elle permet de configurer, de modifier
 * et d'interroger les heures théoriques d'arrivée et de départ pour chaque jour, et fournit des outils de calcul
 * permettant d'évaluer le volume horaire théorique attendu (en minutes) lors d'une journée donnée.
 * Elle implémente {@link Serializable} pour assurer sa sauvegarde sur le disque.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class Schedule implements Serializable {

    @Serial
    /** Identifiant de structure pour la sérialisation et la désérialisation de la classe. */
    private static final long serialVersionUID = 1L;

    /** Table de hachage spécialisée associant chaque jour de la semaine à sa plage horaire (TimeSlot). */
    private EnumMap<DayOfWeek, TimeSlot> horaires;

    /**
     * Constructeur par défaut initialisant un planning complet doté des horaires par défaut.
     * <p>
     * Alloue la structure {@link EnumMap} et appelle la méthode interne {@link #initializeDefault()}
     * pour affecter à chaque jour de la semaine la plage horaire standard définie par {@link TimeSlot}.
     * </p>
     */
    public Schedule() {
        this.horaires = new EnumMap<>(DayOfWeek.class);
        initializeDefault();
    }

    /**
     * Initialise le planning hebdomadaire en appliquant les valeurs temporelles par défaut à tous les jours.
     * <p>
     * Cette méthode privée parcourt l'ensemble des valeurs de l'énumération {@link DayOfWeek} et instancie
     * pour chacune d'elles une copie des heures par défaut de la classe {@link TimeSlot}.
     * </p>
     */
    private void initializeDefault() {
        TimeSlot defaultSlot = new TimeSlot();
        for (DayOfWeek day : DayOfWeek.values()) {
            horaires.put(day, new TimeSlot(defaultSlot.getArrivee(), defaultSlot.getDepart()));
        }
    }

    /**
     * Définit ou modifie la plage horaire d'un jour spécifique de la semaine.
     *
     * @param jour     Le jour de la semaine à configurer (ex: {@code DayOfWeek.MONDAY}).
     * @param timeSlot La nouvelle plage horaire à associer à ce jour.
     * @throws IllegalArgumentException Si le jour ou la plage horaire fournis sont {@code null}.
     */
    public void definirJournee(DayOfWeek jour, TimeSlot timeSlot) {
        if (jour == null || timeSlot == null) {
            throw new IllegalArgumentException("Le jour et la plage horaire ne peuvent pas être null");
        }
        horaires.put(jour, timeSlot);
    }

    /**
     * Récupère la plage horaire associée à un jour précis de la semaine.
     *
     * @param jour Le jour de la semaine à interroger.
     * @return L'objet {@link TimeSlot} correspondant aux horaires de ce jour.
     * @throws IllegalArgumentException Si le jour fourni est {@code null}.
     */
    public TimeSlot getHorairePourJour(DayOfWeek jour) {
        if (jour == null) {
            throw new IllegalArgumentException("Le jour ne peut pas être null");
        }
        return horaires.get(jour);
    }

    /**
     * Récupère l'ensemble de la cartographie des horaires hebdomadaires.
     *
     * @return L'{@link EnumMap} contenant l'intégralité du planning de la semaine.
     */
    public EnumMap<DayOfWeek, TimeSlot> getHoraires() {
        return horaires;
    }

    /**
     * Remplace globalement la structure des horaires hebdomadaires par une nouvelle table.
     *
     * @param horaires La nouvelle {@link EnumMap} d'horaires à appliquer.
     * @throws IllegalArgumentException Si la table fournie est {@code null}.
     */
    public void setHoraires(EnumMap<DayOfWeek, TimeSlot> horaires) {
        if (horaires == null) {
            throw new IllegalArgumentException("Les horaires ne peuvent pas être null");
        }
        this.horaires = horaires;
    }

    /**
     * Évalue l'égalité logique entre ce planning et un autre objet.
     * <p>
     * L'égalité est vérifiée si l'autre objet est également un planning et si sa structure
     * d'horaires interne contient exactement les mêmes associations jour/plage horaire.
     * </p>
     *
     * @param o L'objet à comparer avec l'instance courante.
     * @return {@code true} si les plannings sont identiques, {@code false} sinon.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(horaires, schedule.horaires);
    }

    /**
     * Calcule le code de hachage de l'objet planning.
     * <p>
     * Génère une empreinte numérique basée sur le contenu de la table {@code horaires}
     * pour respecter le contrat d'égalité de {@link #equals(Object)}.
     * </p>
     *
     * @return La valeur entière du hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(horaires);
    }

    /**
     * Génère une représentation textuelle détaillée et structurée du planning hebdomadaire.
     * <p>
     * Parcourt chaque jour configuré de la semaine pour lister de manière lisible les horaires correspondants.
     * </p>
     *
     * @return Une chaîne de caractères contenant le détail jour par jour du planning.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Planning:\n");
        for (Map.Entry<DayOfWeek, TimeSlot> entry : horaires.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Calcule la durée théorique totale de travail attendue pour un jour donné, exprimée en minutes.
     * <p>
     * Cette méthode extrait le {@link TimeSlot} lié au jour demandé, puis mesure l'écart temporel
     * en minutes entre l'heure d'arrivée et l'heure de départ à l'aide de {@link ChronoUnit#MINUTES}.
     * Si aucune plage n'est définie, elle retourne zéro.
     * </p>
     *
     * @param dayOfWeek Le jour de la semaine pour lequel calculer le temps de travail théorique.
     * @return Le nombre total de minutes théoriques dues par l'employé pour ce jour.
     * @throws IllegalArgumentException Si le jour fourni est {@code null}.
     */
    public long getMinutesPourCeJour(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Le jour ne peut pas être null");
        }
        TimeSlot timeSlot = horaires.get(dayOfWeek);
        if (timeSlot == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(timeSlot.getArrivee(), timeSlot.getDepart());
    }
}