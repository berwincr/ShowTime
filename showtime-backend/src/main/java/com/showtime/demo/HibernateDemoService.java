package com.showtime.demo;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plain Hibernate API demo: SessionFactory -> Session -> Transaction.
 * (The main application uses Spring Data JPA, which does all of this for you behind the scenes.)
 */
@Service
public class HibernateDemoService {

    private final EntityManagerFactory emf;

    public HibernateDemoService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Map<String, Object> run() {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);   // one per application
        Map<String, Object> out = new LinkedHashMap<>();

        try (Session session = sessionFactory.openSession()) {               // one per unit of work
            Long count = session.createQuery("select count(s) from Staff s", Long.class).getSingleResult();
            if (count == 0) {
                Transaction tx = session.beginTransaction();                  // one per atomic operation
                try {
                    Manager m = new Manager();
                    m.setName("Ravi Kumar");
                    m.setDepartment("Operations");
                    session.persist(m);

                    Usher u = new Usher();
                    u.setName("Meena");
                    u.setScreenAssigned("Screen 2");
                    session.persist(u);

                    tx.commit();
                    out.put("inserted", "1 Manager + 1 Usher");
                } catch (RuntimeException e) {
                    tx.rollback();
                    throw e;
                }
            }
            // polymorphic HQL query: returns Manager and Usher objects
            List<Staff> all = session.createQuery("from Staff", Staff.class).getResultList();
            out.put("staff", all.stream()
                    .map(s -> s.getClass().getSimpleName() + ": " + s.getName())
                    .toList());
        }
        out.put("note", "Tables created by JOINED inheritance: staff, managers, ushers");
        return out;
    }
}
