package ma.toubkalit.mlia.dao;

import ma.toubkalit.mlia.model.Salle;
import ma.toubkalit.mlia.model.StatutReservation;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SalleDao {

    private final EntityManager em;

    public SalleDao(EntityManager em) {
        this.em = em;
    }

    public void save(Salle salle) {
        em.persist(salle);
    }

    public Optional<Salle> findById(Long id) {
        return Optional.ofNullable(em.find(Salle.class, id));
    }

    public List<Salle> findAll() {
        return em.createQuery("SELECT s FROM Salle s ORDER BY s.nom", Salle.class)
                .getResultList();
    }

    public List<Salle> findByCapaciteMin(int capaciteMin) {
        return em.createQuery("SELECT s FROM Salle s WHERE s.capacite >= :cap ORDER BY s.capacite ASC", Salle.class)
                .setParameter("cap", capaciteMin)
                .setHint("org.hibernate.cacheable", true)
                .getResultList();
    }

    public List<Salle> findSallesDisponibles(LocalDateTime debut, LocalDateTime fin, int capaciteMin) {
        String jpql = "SELECT s FROM Salle s WHERE s.capacite >= :cap AND s.id NOT IN (" +
                "  SELECT r.salle.id FROM Reservation r " +
                "  WHERE r.statut = :statut " +
                "  AND (r.dateDebut < :fin AND r.dateFin > :debut)" +
                ") ORDER BY s.capacite ASC";

        return em.createQuery(jpql, Salle.class)
                .setParameter("cap", capaciteMin)
                .setParameter("statut", StatutReservation.CONFIRMEE)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    public Optional<Salle> findWithDetails(Long id) {
        EntityGraph<?> graph = em.getEntityGraph("Salle.detailComplete");
        List<Salle> results = em.createQuery("SELECT s FROM Salle s WHERE s.id = :id", Salle.class)
                .setParameter("id", id)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
