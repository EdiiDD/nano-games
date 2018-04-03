package es.um.redes.nanoGames.utils;

import java.util.logging.Level;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

	private final static SessionFactory sessionFactory = buildSessionFactory();
	
	
	private static SessionFactory buildSessionFactory() {
		
		try {
			java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);
			StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
					.configure()
					.build();
			SessionFactory sessionFactory = new Configuration().buildSessionFactory(registry);
			return new Configuration().buildSessionFactory(registry);
//	        MetadataSources sources = new MetadataSources(registry);
//	        Metadata metadata = sources.getMetadataBuilder().build();
//	        return metadata.getSessionFactoryBuilder().build();
	        
		} catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
	}
	
	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}

}

