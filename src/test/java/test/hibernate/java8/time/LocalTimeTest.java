package test.hibernate.java8.time;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/15/17.
 */
public class LocalTimeTest {
    private SessionFactory sessionFactory;

    @BeforeEach
    void createSessionFactory() throws Throwable {
        sessionFactory = configuration(LocalTimeEntity.class).buildSessionFactory();
    }

    private Configuration configuration(Class<?>... entityClasses) {
        Configuration cfg = new Configuration();
        Arrays.stream(entityClasses).forEach(cfg::addAnnotatedClass);
        cfg.setProperty(Environment.DIALECT, HSQLDialect.class.getName());
        cfg.setProperty(Environment.URL, "jdbc:hsqldb:mem:test");
        cfg.setProperty(Environment.USER, "sa");
        cfg.setProperty(Environment.HBM2DDL_AUTO, "update");
        cfg.setProperty(Environment.SHOW_SQL, "true");
        cfg.setProperty(Environment.AUTOCOMMIT, "false");
        return cfg;
    }

    @AfterEach
    void closeSessionFactory() throws Throwable {
        sessionFactory.close();
    }

    @Test
    void savingLocalTimeColumn() throws Throwable {
        LocalTime time = LocalTime.of(1, 2, 3);
        LocalTimeEntity entity = new LocalTimeEntity();
        entity.time = time;

        Serializable id = save(entity);

        LocalTimeEntity saved = get(LocalTimeEntity.class, id);
        assertThat(saved.time, equalTo(time));

        String column = getColumn(id);
        assertThat(column, equalTo("01:02:03"));
    }

    private String getColumn(Serializable id) {
        return (String) sessionFactory.openSession()
                .createNativeQuery("select time from LocalTimeEntity where id=:id")
                .setParameter("id", id)
                .uniqueResult();
    }

    private <T> T get(Class<T> entityClass, Serializable id) {
        return sessionFactory.openSession().get(entityClass, id);
    }

    private Serializable save(Object entity) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        try {
            Serializable id = session.save(entity);
            transaction.commit();
            return id;
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    @Entity
    @Table(name = "LocalTimeEntity")
    public static class LocalTimeEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Integer id;
        @Column(columnDefinition = "varchar(8)")
        private LocalTime time;
    }
}
