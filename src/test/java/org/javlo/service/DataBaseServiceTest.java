package org.javlo.service;

import org.javlo.service.database.DataBaseService;
import org.javlo.test.javlo.TestGlobalContext;

import junit.framework.TestCase;

public class DataBaseServiceTest extends TestCase {
	
	public void testConnection() throws Exception {
		TestGlobalContext globalContextTest = new TestGlobalContext();
		globalContextTest.addInConfig("db.url", "jdbc:postgresql://dev2.host.javlo.org/#DB#?characterEncoding=utf8");
		
		assertNotNull(System.getenv().get("DEFAULT_TEST_PASSWORD"));
		assertNotNull(System.getenv().get("DB_TEST_USER"));
		assertNotNull(System.getenv().get("DB_TEST_PASSWORD"));
		
		globalContextTest.addInConfig("db.login",  System.getenv().get("DB_TEST_USER"));
		globalContextTest.addInConfig("db.password", System.getenv().get("DB_TEST_PASSWORD"));
		
		DataBaseService dbService = DataBaseService.getInstance(globalContextTest);
		assertNotNull(dbService);
		assertFalse(dbService.isInternalDb());
		assertNotNull(dbService.getConnection("javlo_form"));
	}

	
}
