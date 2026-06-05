package com.example.mainapp.model.employee;

/**
 * Énumération définissant les différents statuts hiérarchiques et rôles professionnels des employés au sein du système.
 * <p>
 * Ces statuts permettent de catégoriser les collaborateurs au sein du modèle de données et sont conçus pour
 * régir les niveaux d'habilitation ou de visibilité dans l'application centrale (IHM). Par exemple, ils permettent
 * de restreindre l'accès aux onglets de configuration avancée ou aux outils de modification manuelle des pointages
 * aux seuls profils d'administration et de gestion des ressources humaines.
 * </p>
 *
 * @author Youssef M'SADAA, Ahmed DEBBACH, Youssef RIANI, Mohamed Yassine BEN ABDA, Youssef ELYAHYAOUI
 */
public enum Status {

    /** Statut standard représentant un employé classique (Rôle opérationnel sans privilèges d'administration). */
    EMP,

    /** Statut représentant un gestionnaire des Ressources Humaines (Human Resources - habilité à modifier les fiches et les plannings). */
    HRR,

    /** Statut représentant la Direction Générale (Chief Executive Officer - niveau d'accès et de supervision maximal). */
    CEO
}