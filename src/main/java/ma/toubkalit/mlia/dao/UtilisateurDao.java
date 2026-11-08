package ma.toubkalit.mlia.dao;

import ma.toubkalit.mlia.model.Utilisateur;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class UtilisateurDao {

    private final EntityManager em;

    public UtilisateurDao(EntityManager em) {
        this.em = em;
    }

    public void save(Utilisateur user) {
        em.persist(user);
    }

    public Optional<Utilisateur> findById(Long id) {
        return Optional.ofNullable(em.find(Utilisateur.class, id));
    }

    public Optional<Utilisateur> findByEmail(String email) {
        List<Utilisateur> list = em.createQuery("SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                .setParameter("email", email)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Utilisateur> findAll() {
        return em.createQuery("SELECT u FROM Utilisateur u ORDER BY u.nom", Utilisateur.class)
                .getResultList();
    }
}
