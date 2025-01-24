package com.pfem2.iso27004;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DBTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() {
        /* try (var connection = dataSource.getConnection()) {
            System.out.println("DataBase connection successful.");
        }
        ; */
    }

}