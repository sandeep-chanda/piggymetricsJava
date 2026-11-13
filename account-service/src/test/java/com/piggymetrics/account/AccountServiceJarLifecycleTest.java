package com.piggymetrics.account;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * P2.T4: spring-boot-maven-plugin keeps finalName account-service so package
 * writes account-service.jar only after the default Maven test phase.
 */
public class AccountServiceJarLifecycleTest {

	@Test
	public void shouldKeepSpringBootFinalNameAccountService() throws Exception {
		Path pom = Paths.get("pom.xml");
		assertTrue("account-service/pom.xml must be readable from the module build", Files.exists(pom));
		String content = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
		assertTrue(content.contains("<finalName>account-service</finalName>"));
		assertTrue(content.contains("spring-boot-maven-plugin"));
		assertFalse("package must not skip tests via skipTests in pom",
				content.contains("<skipTests>true</skipTests>")
						|| content.contains("<maven.test.skip>true</maven.test.skip>"));
	}
}
