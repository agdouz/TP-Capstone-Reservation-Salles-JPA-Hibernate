# TP Capstone : Finaliser l'Application "Réservation de Salles" avec JPA & Hibernate

> **Auteur :** Mouad Agdouz  
> **GitHub :** [@agdouz](https://github.com/agdouz)  
> **Plateforme :** MLIAEdu - Toubkal IT  
> **Module :** Projet de Synthèse (Capstone) - Architecture d'Entreprise JPA & Hibernate  

---

## 📋 Présentation Générale du Projet Capstone

Ce projet constitue le **Projet de Synthèse (Capstone)** validant l'ensemble des compétences en modélisation et persistance des données avec **JPA (Java Persistence API)** et **Hibernate ORM**.

L'application implémente un système complet de **Réservation de Salles** pour un campus universitaire / centre d'entreprise avec gestion stricte de la concurrence, prévention des conflits d'horaires, optimisation avancée des performances et structuration en couches logicielles professionnelles (Modèle, DAO, Service métier, Tests).

---

## 🌟 Fonctionnalités Implémentées

1. **Modélisation Objet-Relationnelle Complète :**
   - Entités : `Salle`, `Utilisateur`, `Reservation`, `Equipement`.
   - Énumérations JPA (`@Enumerated(EnumType.STRING)`) : `TypeSalle`, `StatutReservation`, `RoleUtilisateur`.
   - Relations JPA : `@OneToMany`, `@ManyToOne`, `@ManyToMany` avec table de jointure personnalisée `salle_equipements`.

2. **Règles Métier & Service de Réservation (`ReservationService`) :**
   - Validation stricte des dates (début < fin, créneaux cohérents).
   - Détection des conflits temporels : interdiction du double-booking sur une même salle via requête JPQL optimisée.
   - Annulation et modification de réservations dans des transactions managées.

3. **Protection contre la Concurrence (Optimistic Locking) :**
   - Versionning automatique de l'entité `Reservation` avec `@Version private Long version;`.
   - Détection des conflits de mise à jour concurrente (`OptimisticLockException`).

4. **Optimisation des Performances & Cache :**
   - Cache de Second Niveau (L2 Cache) configuré avec **Ehcache** (`@Cacheable`, `@Cache(usage = READ_WRITE)`).
   - Cache de Requêtes (Query Cache) pour les recherches de salles fréquentes.
   - Suppression du problème N+1 via requêtes jointes `JOIN FETCH` et graphes d'entités déclaratifs `@NamedEntityGraph`.

---

## 🛠️ Stack Technique

* **Langage :** Java 17 / 21
* **Framework ORM :** Hibernate Core 5.6.5.Final
* **Standard :** JPA 2.2 (`javax.persistence`)
* **Fournisseur Cache :** Ehcache 2.10.9.2
* **Base de données :** H2 In-Memory (`jdbc:h2:mem:capstone_db`)
* **Build Tool :** Apache Maven 3.9+
* **Tests Unitaires :** JUnit 5 Jupiter 5.8.2

---

## 📂 Architecture du Projet

```text
TP-Capstone-Reservation-Salles/
├── pom.xml                                            # Configuration Maven complète
├── run.bat                                            # Script de lancement rapide Windows
├── .gitignore                                         # Fichiers exclus du versionnement
├── README.md                                          # Documentation et rapport de projet
├── src/
│   ├── main/
│   │   ├── java/ma/toubkalit/mlia/
│   │   │   ├── App.java                               # Point d'entrée exécutable avec scénarios
│   │   │   ├── model/                                 # Entités du domaine métier
│   │   │   │   ├── Equipement.java                    # Équipements de salle (Vidéoprojecteur, etc.)
│   │   │   │   ├── Reservation.java                   # Réservation avec @Version
│   │   │   │   ├── RoleUtilisateur.java               # Enum des rôles
│   │   │   │   ├── Salle.java                         # Entité Salle avec @NamedEntityGraph
│   │   │   │   ├── StatutReservation.java             # Enum des statuts de réservation
│   │   │   │   └── TypeSalle.java                     # Enum des types de salles
│   │   │   ├── dao/                                   # Couche d'Accès aux Données (DAO)
│   │   │   │   ├── ReservationDao.java
│   │   │   │   ├── SalleDao.java
│   │   │   │   └── UtilisateurDao.java
│   │   │   └── service/                               # Couche Métier / Transactions
│   │   │       ├── ConflictReservationException.java  # Exception métier dédiée aux conflits
│   │   │       └── ReservationService.java            # Service de réservation et règles métier
│   │   └── resources/
│   │       ├── ehcache.xml                            # Paramétrage des régions de cache
│   │       └── META-INF/
│   │           └── persistence.xml                    # Configuration JPA / Hibernate / H2
│   └── test/
│       └── java/ma/toubkalit/mlia/
│           └── ReservationServiceTest.java            # Suite de tests unitaires JUnit 5
```

---

## 🚀 Compilation, Tests et Exécution

### 1. Compiler et lancer l'application console
```bash
mvn clean compile exec:java
```
Ou double-cliquer sur `run.bat`.

### 2. Exécuter la suite de tests automatisés JUnit 5
```bash
mvn test
```
Résultat :
```text
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
