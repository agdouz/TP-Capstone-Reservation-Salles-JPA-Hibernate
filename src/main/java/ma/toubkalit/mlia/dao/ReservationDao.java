package ma.toubkalit.mlia.dao;

import ma.toubkalit.mlia.model.Reservation;
import ma.toubkalit.mlia.model.StatutReservation;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationDao {

    private final EntityManager em;

    public ReservationDao(EntityManager em) {
        this.em = em;
    }

    public void save(Reservation reservation) {
        em.persist(reservation);
    }

    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(em.find(Reservation.class, id));
    }

    public List<Reservation> findBySalle(Long salleId) {
        return em.createQuery("SELECT r FROM Reservation r WHERE r.salle.id = :salleId ORDER BY r.dateDebut", Reservation.class)
                .setParameter("salleId", salleId)
                .getResultList();
    }

    public List<Reservation> findByUtilisateur(Long userId) {
        return em.createQuery("SELECT r FROM Reservation r WHERE r.utilisateur.id = :userId ORDER BY r.dateDebut", Reservation.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Reservation> findOverlappingReservations(Long salleId, LocalDateTime debut, LocalDateTime fin) {
        String jpql = "SELECT r FROM Reservation r " +
                "WHERE r.salle.id = :salleId " +
                "AND r.statut = :statut " +
                "AND (r.dateDebut < :fin AND r.dateFin > :debut)";

        return em.createQuery(jpql, Reservation.class)
                .setParameter("salleId", salleId)
                .setParameter("statut", StatutReservation.CONFIRMEE)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    public List<Reservation> findAllWithDetails() {
        return em.createQuery("SELECT DISTINCT r FROM Reservation r JOIN FETCH r.salle JOIN FETCH r.utilisateur ORDER BY r.dateDebut", Reservation.class)
                .getResultList();
    }
}
