package com.piggymetrics.account;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * P3.T2: Gate that the five parity classes remain on account-service and are
 * runnable via mvn test before the independent image build.
 */
public class IndependentImageParityTest {

	private static final String[] PARITY_CLASSES = {
			"com.piggymetrics.account.AccountServiceApplicationTests",
			"com.piggymetrics.account.client.StatisticsServiceClientFallbackTest",
			"com.piggymetrics.account.controller.AccountControllerTest",
			"com.piggymetrics.account.repository.AccountRepositoryTest",
			"com.piggymetrics.account.service.AccountServiceTest"
	};

	private static final String[] PARITY_SOURCES = {
			"src/test/java/com/piggymetrics/account/AccountServiceApplicationTests.java",
			"src/test/java/com/piggymetrics/account/client/StatisticsServiceClientFallbackTest.java",
			"src/test/java/com/piggymetrics/account/controller/AccountControllerTest.java",
			"src/test/java/com/piggymetrics/account/repository/AccountRepositoryTest.java",
			"src/test/java/com/piggymetrics/account/service/AccountServiceTest.java"
	};

	@Test
	public void shouldKeepFiveParityTestClassesOnAccountService() throws Exception {
		for (int i = 0; i < PARITY_CLASSES.length; i++) {
			Path source = Paths.get(PARITY_SOURCES[i]);
			assertTrue("parity source must exist: " + PARITY_SOURCES[i], Files.exists(source));
			Class<?> loaded = Class.forName(PARITY_CLASSES[i]);
			assertNotNull(loaded);
			assertTrue(loaded.getName().endsWith("Test") || loaded.getName().endsWith("Tests"));
		}
	}
}
