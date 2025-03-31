package com.example.services;

import com.example.dao.AuthDao;
import com.example.dao.LocationDao;
import com.example.dto.response.WeatherResponseDto;
import com.example.models.Locations;
import com.example.models.Users;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationDao locationDao;
    private final AuthDao authDao;

    public void saveLocation(Integer id, String city, double lat, double lon) {

        if (id == null) {
            throw new IllegalArgumentException("id не может быть null");
        }

        Users user = authDao.findById(id);

        Locations locations = new Locations();
        locations.setUser(user);
        locations.setName(city);
        locations.setLatitude(lat);
        locations.setLongitude(lon);

        locationDao.save(locations);

    }


}
