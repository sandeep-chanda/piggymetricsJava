package com.piggymetrics.account;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * P3.T3: Build the account-service image from account-service/Dockerfile
 * after package produces target/account-service.jar.
 */
public class AccountServiceImageBuildTest {

	@Test
	public void shouldDefineIndependentImageBuildFromModuleDockerfile() throws Exception {
		Path dockerfile = Paths.get("Dockerfile");
		assertTrue(Files.exists(dockerfile));
		String content = new String(Files.readAllBytes(dockerfile), StandardCharsets.UTF_8);
		assertTrue(content.contains("FROM eclipse-temurin:17-jre"));
		assertTrue(content.contains("account-service.jar"));
		assertTrue(content.contains("EXPOSE 6000"));
		assertTrue("image build must not require repo-root compose inputs",
				!content.toLowerCase().contains("docker-compose"));
	}

	@Test
	public void shouldBuildAccountServiceLocalImageWhenDockerAndJarAvailable() throws Exception {
		Path jar = Paths.get("target/account-service.jar");
		assumeTrue("account-service.jar must exist from mvn package", Files.exists(jar));
		assumeTrue("docker CLI must be available", isDockerAvailable());

		ProcessBuilder pb = new ProcessBuilder("docker", "build", "-t", "account-service:local", ".");
		pb.directory(Paths.get(".").toAbsolutePath().normalize().toFile());
		pb.redirectErrorStream(true);
		Process process = pb.start();
		String output;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			output = reader.lines().collect(Collectors.joining("\n"));
		}
		boolean finished = process.waitFor(10, TimeUnit.MINUTES);
		assertTrue("docker build timed out", finished);
		assertEquals("docker build failed:\n" + output, 0, process.exitValue());
	}

	private static boolean isDockerAvailable() {
		try {
			Process process = new ProcessBuilder("docker", "info").redirectErrorStream(true).start();
			boolean finished = process.waitFor(30, TimeUnit.SECONDS);
			return finished && process.exitValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}
}
