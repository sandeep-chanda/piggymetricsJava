package com.piggymetrics.account;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * P3.T1: account-service/Dockerfile packages the runnable jar from
 * account-service/pom.xml and launches AccountApplication on Java 17
 * without repository-level compose or deployment manifests.
 */
public class AccountServiceDockerfileTest {

	@Test
	public void shouldPackageAccountServiceJarOnEclipseTemurin17() throws Exception {
		Path dockerfile = Paths.get("Dockerfile");
		assertTrue("account-service/Dockerfile must exist", Files.exists(dockerfile));
		String content = new String(Files.readAllBytes(dockerfile), StandardCharsets.UTF_8);

		assertTrue("Dockerfile must use eclipse-temurin:17-jre",
				content.contains("FROM eclipse-temurin:17-jre"));
		assertTrue("Dockerfile must ADD target/account-service.jar",
				content.contains("ADD ./target/account-service.jar /app/")
						|| content.contains("ADD ./target/account-service.jar /app"));
		assertTrue("CMD must launch the Spring Boot jar",
				content.contains("/app/account-service.jar"));
		assertTrue("CMD must invoke java -jar style launch",
				content.contains("-jar"));
		assertTrue("Dockerfile must EXPOSE 6000", content.contains("EXPOSE 6000"));

		assertFalse("Dockerfile must not depend on docker-compose.yml",
				content.toLowerCase().contains("docker-compose"));
		assertFalse("Dockerfile must not reference gateway manifests",
				content.toLowerCase().contains("gateway"));
		assertFalse("Dockerfile must not stay on java:8-jre",
				content.contains("java:8") || content.contains("FROM java:"));
	}
}
