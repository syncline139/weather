package com.example.dao;

import com.example.models.Locations;
import com.example.models.Sessions;
import com.example.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Repository
@Transactional
@RequiredArgsConstructor
public class LocationDao {


    private final SessionFactory sessionFactory;


    public void save(Locations locations) {
        if (locations == null) {
            throw new IllegalArgumentException("Объект Locations не может быть null");
        }
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.persist(locations);
    }

    public boolean uniqueLocationDate(double lat, double lon, int id) {
        Session currentSession = sessionFactory.getCurrentSession();
        Integer result = currentSession
                .createQuery("select l.id from Locations l where l.latitude = :lat and l.longitude = :lon and l.user.id = :id", Integer.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("id", id)
                .getSingleResultOrNull();

        return result != null;
    }


}
