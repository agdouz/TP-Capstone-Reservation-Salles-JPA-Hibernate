package ma.toubkalit.mlia;

import ma.toubkalit.mlia.dao.ReservationDao;
import ma.toubkalit.mlia.dao.SalleDao;
import ma.toubkalit.mlia.dao.UtilisateurDao;
import ma.toubkalit.mlia.model.*;
import ma.toubkalit.mlia.service.ConflictReservationException;
import ma.toubkalit.mlia.service.ReservationService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Projet Capstone : Application complète de gestion des réservations de salles avec JPA et Hibernate.
 *
 * @author Mouad Agdouz (agdouz)
 */
public class App {

    private static final String PU_NAME = "capstone-pu";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        afficherBanniere();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PU_NAME);

        try {
            ReservationService reservationService = new ReservationService(emf);

            // 1. Initialisation du Référentiel
            Long salle1Id = initialiserReferentiel(emf);

            // 2. Création de réservations valides
            System.out.println("\n--- [DÉMONSTRATION 1] : Création de Réservations Valides ---");
            LocalDateTime demain9h = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
            LocalDateTime demain12h = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);

            Reservation r1 = reservationService.creerReservation(
                    salle1Id, 1L, demain9h, demain12h, "Comité de Direction IA"
            );
            System.out.println("✔ Réservation créée avec succès ! ID: " + r1.getId() 
                    + " | Salle: " + r1.getSalle().getNom() + " | Statut: " + r1.getStatut());

            // 3. Détection et Rejet de conflit de chevauchement (Double-Booking Prevention)
            System.out.println("\n--- [DÉMONSTRATION 2] : Détection de Conflit (Chevauchement Temporel) ---");
            LocalDateTime chevauchementDebut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime chevauchementFin = LocalDateTime.now().plusDays(1).withHour(11).withMinute(30);

            try {
                System.out.println("Tentative de réservation concurrente sur le même créneau (10h00 - 11h30)...");
                reservationService.creerReservation(
                        salle1Id, 2L, chevauchementDebut, chevauchementFin, "Réunion en Conflit"
                );
                System.err.println("❌ Erreur : Le conflit aurait dû être intercepté !");
            } catch (ConflictReservationException e) {
                System.out.println("✔ Conflit bloqué avec succès par le Service métier :");
                System.out.println("   Message d'erreur métier : " + e.getMessage());
            }

            // 4. Recherche avancée de salles disponibles
            System.out.println("\n--- [DÉMONSTRATION 3] : Recherche Multicritère de Salles Disponibles ---");
            System.out.println("Recherche de salles disponibles pour demain 14h00-16h00 (Capacité minimale: 20 personnes) :");
            List<Salle> disponibles = reservationService.rechercherSallesDisponibles(
                    LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                    LocalDateTime.now().plusDays(1).withHour(16).withMinute(0),
                    20
            );
            for (Salle s : disponibles) {
                System.out.println("   -> [DISPONIBLE] " + s.getNom() + " (Capacité: " + s.getCapacite() 
                        + ", Bâtiment: " + s.getBatiment() + ", Type: " + s.getTypeSalle() + ")");
            }

            // 5. Test du Verrouillage Optimiste (@Version) sur Réservation
            System.out.println("\n--- [DÉMONSTRATION 4] : Protection de Concurrence (@Version) ---");
            demontrerOptimisticLocking(emf, r1.getId());

            // 6. Affichage récapitulatif du Planning
            System.out.println("\n--- [DÉMONSTRATION 5] : Planning Global des Réservations ---");
            afficherPlanningComplet(emf);

        } finally {
            if (emf.isOpen()) {
                emf.close();
            }
        }
    }

    private static void afficherBanniere() {
        System.out.println("===============================================================================");
        System.out.println("   CAPSTONE PROJECT : GESTION DE RÉSERVATION DE SALLES (JPA & HIBERNATE)");
        System.out.println("   Auteur : Mouad Agdouz | GitHub : https://github.com/agdouz");
        System.out.println("   Plateforme : MLIAEdu - Toubkal IT");
        System.out.println("===============================================================================\n");
    }

    private static Long initialiserReferentiel(EntityManagerFactory emf) {
        System.out.println(">>> [INITIALISATION] : Création des Salles, Équipements et Utilisateurs...");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        // Équipements
        Equipement eq1 = new Equipement("Vidéoprojecteur 4K Laser", "Ultra haute définition interactif");
        Equipement eq2 = new Equipement("Système Vidéoconférence Polycom", "Microphones haute fidélité");
        Equipement eq3 = new Equipement("Serveurs GPU IA Nvidia", "Station d'entraînement local");
        em.persist(eq1);
        em.persist(eq2);
        em.persist(eq3);

        // Salles
        Salle s1 = new Salle("Salle Alan Turing", 45, "Bâtiment Informatique", "1er étage", TypeSalle.CONFERENCE);
        s1.addEquipement(eq1);
        s1.addEquipement(eq2);

        Salle s2 = new Salle("Laboratoire Vision & IA", 30, "Bâtiment Recherche", "Rez-de-chaussée", TypeSalle.LABORATOIRE_IA);
        s2.addEquipement(eq1);
        s2.addEquipement(eq3);

        Salle s3 = new Salle("Salle Réunion Agora", 15, "Bâtiment Direction", "2ème étage", TypeSalle.REUNION);
        s3.addEquipement(eq2);

        em.persist(s1);
        em.persist(s2);
        em.persist(s3);

        // Utilisateurs
        Utilisateur u1 = new Utilisateur("Mouad Agdouz", "mouad.agdouz@mlia.com", "Data & IA", RoleUtilisateur.CHERCHEUR);
        Utilisateur u2 = new Utilisateur("Dr. Karim Benjelloun", "karim.ben@mlia.com", "Informatique", RoleUtilisateur.ENSEIGNANT);
        Utilisateur u3 = new Utilisateur("Directrice des Études", "admin@mlia.com", "Direction", RoleUtilisateur.ADMINISTRATEUR);

        em.persist(u1);
        em.persist(u2);
        em.persist(u3);

        em.getTransaction().commit();
        em.close();
        System.out.println("✔ Référentiel initialisé avec succès !");
        return s1.getId();
    }

    private static void demontrerOptimisticLocking(EntityManagerFactory emf, Long reservationId) {
        EntityManager emA = emf.createEntityManager();
        EntityManager emB = emf.createEntityManager();

        try {
            Reservation resA = emA.find(Reservation.class, reservationId);
            Reservation resB = emB.find(Reservation.class, reservationId);

            // Session A modifie le motif et commit
            emA.getTransaction().begin();
            resA.setMotif("Comité IA - Mis à jour par Session A");
            emA.getTransaction().commit();
            System.out.println("✔ [Session A] Commit réussi -> Nouvelle version en BD : " + resA.getVersion());

            // Session B essaie de commit avec l'ancienne version
            emB.getTransaction().begin();
            resB.setMotif("Conflit de version par Session B");
            try {
                emB.getTransaction().commit();
            } catch (RollbackException | OptimisticLockException e) {
                System.out.println("✔ [Session B] Conflit de version intercepté par OptimisticLockException !");
                if (emB.getTransaction().isActive()) emB.getTransaction().rollback();
            }
        } finally {
            emA.close();
            emB.close();
        }
    }

    private static void afficherPlanningComplet(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            ReservationDao dao = new ReservationDao(em);
            List<Reservation> reservations = dao.findAllWithDetails();

            System.out.println("-------------------------------------------------------------------------------------------------");
            System.out.printf("%-5s | %-25s | %-20s | %-28s | %-12s%n", "ID", "Salle", "Demandeur", "Créneau", "Statut");
            System.out.println("-------------------------------------------------------------------------------------------------");

            for (Reservation r : reservations) {
                String creneau = r.getDateDebut().format(FORMATTER) + " -> " + r.getDateFin().format(FORMATTER);
                System.out.printf("%-5d | %-25s | %-20s | %-28s | %-12s%n",
                        r.getId(),
                        r.getSalle().getNom(),
                        r.getUtilisateur().getNom(),
                        creneau,
                        r.getStatut());
            }
            System.out.println("-------------------------------------------------------------------------------------------------");
        } finally {
            em.close();
        }
    }
}
