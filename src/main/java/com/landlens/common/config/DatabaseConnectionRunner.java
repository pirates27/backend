package com.landlens.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Validates and tests the MySQL database connection on Spring Boot startup.
 */
@Component
public class DatabaseConnectionRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionRunner.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        log.info("Testing MySQL Database Connection to remote host...");
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            String url = connection.getMetaData().getURL();
            String username = connection.getMetaData().getUserName();
            log.info("========================================================================");
            log.info("DATABASE CONNECTION SUCCESSFUL!");
            log.info("Connected JDBC URL: {}", url);
            log.info("Active Database Catalog: {}", catalog);
            log.info("Active DB User: {}", username);
            log.info("========================================================================");
        } catch (Exception e) {
            log.error("========================================================================");
            log.error("DATABASE CONNECTION FAILED! Please check host, port, credentials and network permissions.", e);
            log.error("========================================================================");
        }
    }
}
