package ma.toubkalit.mlia;

import ma.toubkalit.mlia.model.*;
import ma.toubkalit.mlia.service.ConflictReservationException;
import ma.toubkalit.mlia.service.ReservationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {

    private static EntityManagerFactory emf;
    private static ReservationService reservationService;
    private static Long salleId;
    private static Long userId;

    @BeforeAll
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("capstone-pu");
        reservationService = new ReservationService(emf);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Salle salle = new Salle("Salle Test Alpha", 30, "Bat A", "RDC", TypeSalle.REUNION);
        em.persist(salle);

        Utilisateur user = new Utilisateur("Testeur", "test@mlia.com", "IT", RoleUtilisateur.ENSEIGNANT);
        em.persist(user);

        em.getTransaction().commit();
        salleId = salle.getId();
        userId = user.getId();
        em.close();
    }

    @AfterAll
    public static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @DisplayName("Test de création d'une réservation valide")
    public void testCreerReservationValide() {
        LocalDateTime debut = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
        LocalDateTime fin = LocalDateTime.now().plusDays(5).withHour(12).withMinute(0);

        Reservation res = reservationService.creerReservation(salleId, userId, debut, fin, "Réunion Test");

        assertNotNull(res);
        assertNotNull(res.getId());
        assertEquals(StatutReservation.CONFIRMEE, res.getStatut());
        assertEquals(0L, res.getVersion());
    }

    @Test
    @DisplayName("Test d'interception d'un conflit de réservation (Double-booking)")
    public void testConflitReservation() {
        LocalDateTime debut = LocalDateTime.now().plusDays(6).withHour(14).withMinute(0);
        LocalDateTime fin = LocalDateTime.now().plusDays(6).withHour(16).withMinute(0);

        // 1ère réservation réussie
        reservationService.creerReservation(salleId, userId, debut, fin, "Session 1");

        // 2ème réservation qui chevauche (15h - 17h)
        LocalDateTime chevaucheDebut = LocalDateTime.now().plusDays(6).withHour(15).withMinute(0);
        LocalDateTime chevaucheFin = LocalDateTime.now().plusDays(6).withHour(17).withMinute(0);

        assertThrows(ConflictReservationException.class, () -> {
            reservationService.creerReservation(salleId, userId, chevaucheDebut, chevaucheFin, "Session Conflit");
        });
    }

    @Test
    @DisplayName("Test de recherche de salles disponibles")
    public void testRechercherSallesDisponibles() {
        LocalDateTime debut = LocalDateTime.now().plusDays(10).withHour(8).withMinute(0);
        LocalDateTime fin = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);

        List<Salle> disponibles = reservationService.rechercherSallesDisponibles(debut, fin, 20);
        assertFalse(disponibles.isEmpty());
        assertTrue(disponibles.stream().anyMatch(s -> s.getId().equals(salleId)));
    }
}
