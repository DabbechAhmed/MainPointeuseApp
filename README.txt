==============================================================================
PROJET TUTORÉ JAVA - SYSTEME DE POINTAGE (POLYTECH TOURS - JUIN 2026)
==============================================================================
Membres : M.Y. BEN ABDA, Y. M'SADAA, A. DEBBACH, Y. RIANI, Y. ELYAHYAOUI.

1. PRÉREQUIS TECHNIQUES & RÈGLES DE LANCEMENT
- Environnement : **JDK Amazon Corretto 22.0.2** et modules **JavaFX 21**.
- Ordre de lancement obligatoire :
  1. Executer la classe `MainApp` (Serveur central RH, port TCP par défaut) à partir de la classe Launcher.
  2. Executer la classe `PointeuseApp` (Terminal client de pointage) à partir de la classe Launcher.
Note : -Mode hors-ligne automatique avec cache local ("pending.ser") et
resynchronisation en arrière-plan toutes les 15s si le serveur est coupé.
-L'application gére plusieurs pointages (Option).

2. STRUCTURE DES SOURCES (ARCHITECTURE MVC)
- com.example.dto : DTOs sérialisables partagés (Langage d'échange réseau).
- com.example.mainapp (Serveur) : Modèle (`Company`), Services Singletons,
  `ClientHandler` (Thread TCP) et Vues d'administration JavaFX.
- com.example.pointeuseapp (Client) : IHM graphique de la pointeuse.

3. LIENS DES LIVRABLES (DOCUMENTS & VIDÉO)
- Vidéo Démo (3 min) : https://www.youtube.com/watch?v=ncT7jskhyTc
- Diagramme de classes : Dans le dossier racine `diagramme_classes.pdf`
- Maquette de l'IHM : Dans le dossier racine `maquette_ihm.pdf`

4. CONTRIBUTIONS INDIVIDUELLES (REPARTITION PAR LOTS)
- A. DEBBACH : Sockets TCP, ClientHandler, Couche Services Singletons,
  Filtre multicritères des pointages (Option) et Import global CSV (Option).
- M.Y. BEN ABDA : Architecture IHM Serveur JavaFX (Main/Tableaux/Fenêtres).
- Y. M'SADAA : Application de pointeuse (Client) et Sérialisation des
  paramètres réseau dans un fichier de configuration locale (Option).
- Y. RIANI : Gestion CRUD Employés/Départements et Modèle Schedule/TimeSlot.
- Y. ELYAHYAOUI : Persistance centrale (company_data.ser) et Logique Métier et maquettage.
==============================================================================