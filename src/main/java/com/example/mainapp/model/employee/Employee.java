package com.example.mainapp.model.employee;

import com.example.mainapp.model.department.Department;
import com.example.mainapp.model.schedule.Schedule;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Représente un employé au sein de l'entreprise et centralise ses données personnelles et professionnelles.
 * <p>
 * Cette classe de domaine encapsule l'identité d'un collaborateur (ID, nom, prénom), son statut
 * hiérarchique, son département d'affectation, ainsi que son planning de travail hebdomadaire.
 * Elle assure également le suivi en temps réel de son solde d'heures supplémentaires (converti en minutes).
 * Elle implémente {@link Serializable} pour permettre la persistance des fiches employés sur le disque.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public class Employee implements Serializable {

    @Serial
    /** Identifiant de structure pour la sérialisation et la désérialisation de la classe. */
    private static final long serialVersionUID = 1L;

    /** Identifiant unique et immuable de l'employé, généré sous forme d'UUID. */
    private UUID id;

    /** Le prénom de l'employé. */
    private String name;

    /** Le nom de famille de l'employé. */
    private String surname;

    /** Le statut ou rôle de l'employé au sein de l'organisation (ex: EMP, RH). */
    private Status status;

    /** Le département ou service auquel l'employé est rattaché. */
    private Department department;

    /** Le planning hebdomadaire fixe contenant les horaires théoriques de l'employé. */
    private Schedule schedule;

    /** Le solde cumulé des heures supplémentaires ou de retard, exprimé en minutes (positif ou négatif). */
    private long soldeMinutes;

    /**
     * Constructeur par défaut initialisant un employé vierge avec des valeurs par défaut.
     * <p>
     * Génère automatiquement un identifiant unique {@link UUID}, initialise un planning par défaut
     * et configure le solde initial de minutes à zéro.
     * </p>
     */
    public Employee() {
        this.id = UUID.randomUUID();
        this.name = "";
        this.surname = "";
        this.status = Status.EMP;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    /**
     * Construit un employé complet en injectant ses informations d'identité et d'affectation principales.
     * <p>
     * Initialise automatiquement un identifiant unique {@link UUID}, instancie un nouveau planning vide
     * et configure le solde initial à zéro minute.
     * </p>
     *
     * @param department Le département d'affectation de l'employé.
     * @param name       Le prénom de l'employé.
     * @param surname    Le nom de famille de l'employé.
     * @param status     Le statut ou rôle professionnel attribué.
     */
    public Employee(Department department, String name, String surname, Status status) {
        this.id = UUID.randomUUID();
        this.department = department;
        this.name = name;
        this.surname = surname;
        this.status = status;
        this.schedule = new Schedule();
        this.soldeMinutes = 0L;
    }

    /**
     * Modifie le solde de minutes cumulées en ajoutant ou soustrayant la valeur fournie.
     *
     * @param minutes Le nombre de minutes à ajouter au solde (peut être négatif en cas de retard).
     */
    public void modifierSoldeMinutes(long minutes) {
        this.soldeMinutes += minutes;
    }

    /**
     * Récupère l'identifiant unique de l'employé.
     *
     * @return L'identifiant {@link UUID} associé à l'employé.
     */
    public UUID getId() { return id; }

    /**
     * Définit ou force l'identifiant unique de l'employé.
     *
     * @param id Le nouvel identifiant {@link UUID} à appliquer.
     */
    public void setId(UUID id) { this.id = id; }

    /**
     * Récupère le département d'affectation de l'employé.
     *
     * @return L'objet {@link Department} représentant son service actuel.
     */
    public Department getDepartment() { return department; }

    /**
     * Affecte l'employé à un nouveau département.
     *
     * @param department Le nouveau dossier {@link Department} d'affectation.
     */
    public void setDepartment(Department department) { this.department = department; }

    /**
     * Récupère le prénom de l'employé.
     *
     * @return La chaîne de caractères correspondant au prénom.
     */
    public String getName() { return name; }

    /**
     * Modifie le prénom de l'employé.
     *
     * @param name Le nouveau prénom à enregistrer.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Récupère le nom de famille de l'employé.
     *
     * @return La chaîne de caractères correspondant au nom de famille.
     */
    public String getSurname() { return surname; }

    /**
     * Modifie le nom de famille de l'employé.
     *
     * @param surname Le nouveau nom de famille à enregistrer.
     */
    public void setSurname(String surname) { this.surname = surname; }

    /**
     * Récupère le statut/rôle professionnel de l'employé.
     *
     * @return L'énumération {@link Status} de l'employé.
     */
    public Status getStatus() { return status; }

    /**
     * Modifie le statut/rôle professionnel de l'employé.
     *
     * @param status Le nouveau {@link Status} à attribuer.
     */
    public void setStatus(Status status) { this.status = status; }

    /**
     * Récupère le planning de travail de l'employé.
     *
     * @return L'objet {@link Schedule} contenant la grille des horaires de la semaine.
     */
    public Schedule getSchedule() { return schedule; }

    /**
     * Assigne un nouveau planning de travail à l'employé.
     *
     * @param schedule Le nouveau planning {@link Schedule} à appliquer.
     */
    public void setSchedule(Schedule schedule) { this.schedule = schedule; }

    /**
     * Récupère le solde actuel d'heures supplémentaires, exprimé en minutes.
     *
     * @return Le nombre total de minutes en crédit (positif) ou en débit (négatif).
     */
    public long getSoldeMinutes() { return soldeMinutes; }

    /**
     * Définit directement la valeur absolue du solde de minutes de l'employé.
     *
     * @param soldeMinutes Le nouveau total de minutes à affecter au solde.
     */
    public void setSoldeMinutes(long soldeMinutes) { this.soldeMinutes = soldeMinutes; }

    /**
     * Retourne une représentation textuelle lisible et synthétique de l'employé.
     * <p>
     * Combine le prénom, le nom et le nom du département d'affectation entre crochets.
     * </p>
     *
     * @return Une chaîne de caractères formatée représentant l'employé.
     */
    @Override
    public String toString() {
        String deptName = (department != null) ? department.getName() : "Aucun";
        return name + " " + surname + " [" + deptName + "]";
    }

    /**
     * Évalue l'égalité logique entre cet employé et un autre objet.
     * <p>
     * L'égalité est strictement basée sur la comparaison des identifiants uniques (UUID).
     * </p>
     *
     * @param object L'objet à comparer avec l'instance courante.
     * @return {@code true} si l'objet est un employé doté du même UUID, {@code false} sinon.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || this.getClass() != object.getClass()) return false;
        Employee emp = (Employee) object;
        return id.equals(emp.id);
    }

    /**
     * Calcule l'empreinte numérique (Hash Code) de l'objet employé.
     * <p>
     * Ce hash est calculé exclusivement à partir de l'identifiant unique (UUID)
     * pour rester en parfaite cohérence avec la méthode {@link #equals(Object)}.
     * </p>
     *
     * @return La valeur entière du hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Effectue une copie complète par valeur des attributs d'un autre employé dans l'instance courante.
     * <p>
     * Cette méthode permet de mettre à jour l'ensemble des données d'une fiche employé (y compris son UUID,
     * son planning et son solde de minutes) sans rompre la référence de l'objet d'origine.
     * </p>
     *
     * @param employee L'employé source contenant les données à copier.
     */
    public void setEmployee(Employee employee) {
        if (employee != null) {
            this.id = employee.id;
            this.department = employee.department;
            this.name = employee.name;
            this.surname = employee.surname;
            this.status = employee.status;
            this.schedule = employee.schedule;
            this.soldeMinutes = employee.soldeMinutes;
        }
    }
}