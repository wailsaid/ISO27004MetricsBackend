package com.pfem2.iso27004;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@SpringBootTest
class Iso27004ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDatabaseConnection() {
		// Create an in-memory H2 database configuration for testing
		EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.H2)
			.addScript("schema.sql")  // Optional: Add your schema creation script
			.addScript("test-data.sql")  // Optional: Add your test data script
			.build();

		try {
			// Test the connection
			Connection connection = database.getConnection();
			assertNotNull(connection, "Database connection should not be null");
			assertTrue(connection.isValid(1), "Database connection should be valid");
			
			// Clean up
			connection.close();
			database.shutdown();
		} catch (SQLException e) {
			fail("Database connection test failed: " + e.getMessage());
		}
	}

}
