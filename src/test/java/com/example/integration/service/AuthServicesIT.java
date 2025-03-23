package com.example.integration.service;

import com.example.config.SpringConfig;
import com.example.integration.annotation.IT;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@IT
@RequiredArgsConstructor
public class AuthServicesIT {



    @Test
    void test() {
    }
}
