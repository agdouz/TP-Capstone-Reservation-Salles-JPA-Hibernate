package ma.toubkalit.mlia.service;

import ma.toubkalit.mlia.dao.ReservationDao;
import ma.toubkalit.mlia.dao.SalleDao;
import ma.toubkalit.mlia.dao.UtilisateurDao;
import ma.toubkalit.mlia.model.Reservation;
import ma.toubkalit.mlia.model.Salle;
import ma.toubkalit.mlia.model.StatutReservation;
import ma.toubkalit.mlia.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationService {

    private final EntityManagerFactory emf;

    public ReservationService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Reservation creerReservation(Long salleId, Long utilisateurId, LocalDateTime debut, LocalDateTime fin, String motif) {
        if (fin.isBefore(debut) || fin.isEqual(debut)) {
            throw new IllegalArgumentException("La date de fin doit être strictement postérieure à la date de début.");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            SalleDao salleDao = new SalleDao(em);
            ReservationDao reservationDao = new ReservationDao(em);
            UtilisateurDao utilisateurDao = new UtilisateurDao(em);

            Salle salle = salleDao.findById(salleId)
                    .orElseThrow(() -> new IllegalArgumentException("Salle introuvable avec ID: " + salleId));

            Utilisateur utilisateur = utilisateurDao.findById(utilisateurId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec ID: " + utilisateurId));

            // Contrôle des conflits temporels (Overlapping check)
            List<Reservation> conflicts = reservationDao.findOverlappingReservations(salleId, debut, fin);
            if (!conflicts.isEmpty()) {
                throw new ConflictReservationException("Conflit détecté : La salle '" + salle.getNom() 
                        + "' est déjà réservée sur ce créneau (" + debut + " à " + fin + ").");
            }

            Reservation reservation = new Reservation(debut, fin, motif, StatutReservation.CONFIRMEE, salle, utilisateur);
            reservationDao.save(reservation);

            tx.commit();
            return reservation;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void annulerReservation(Long reservationId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            ReservationDao reservationDao = new ReservationDao(em);
            Reservation res = reservationDao.findById(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable avec ID: " + reservationId));

            res.setStatut(StatutReservation.ANNULEE);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Salle> rechercherSallesDisponibles(LocalDateTime debut, LocalDateTime fin, int capaciteMin) {
        EntityManager em = emf.createEntityManager();
        try {
            SalleDao salleDao = new SalleDao(em);
            return salleDao.findSallesDisponibles(debut, fin, capaciteMin);
        } finally {
            em.close();
        }
    }
}
