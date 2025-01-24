package com.pfem2.iso27004.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${database_host}")
    private String host;

    @Value("${database_port}")
    private String port;

    @Value("${database_name}")
    private String database;

    @Value("${database_user}")
    private String username;

    @Value("${database_password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(String.format("jdbc:mysql://{}:{}{}", host, port, database));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Test the connection
        try {
            dataSource.getConnection().close();
            logger.info("Database connection successful!");
        } catch (Exception e) {
            logger.error("Database connection failed: " + e.getMessage());
            // Handle the failure (e.g., log the error, use a fallback, etc.)
        }

        return dataSource;
    }
}
