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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * P3.T4: Image starts with MongoDB, OAuth, AUTH_SERVICE_URL, and
 * STATISTICS_SERVICE_URL env values; Spring reaches running state without
 * Eureka or the shared config server.
 */
public class AccountServiceIndependentStartTest {

	@Test
	public void shouldExposeEnvDrivenServiceOwnedSettingsForIndependentStart() throws Exception {
		Path applicationYml = Paths.get("src/main/resources/application.yml");
		assertTrue(Files.exists(applicationYml));
		String application = new String(Files.readAllBytes(applicationYml), StandardCharsets.UTF_8);
		assertTrue(application.contains("MONGODB_HOST"));
		assertTrue(application.contains("MONGODB_PASSWORD"));
		assertTrue(application.contains("ACCOUNT_SERVICE_PASSWORD"));
		assertTrue(application.contains("AUTH_SERVICE_URL"));
		assertTrue(application.contains("STATISTICS_SERVICE_URL"));
		assertTrue(application.contains("RABBITMQ_HOST"));
		assertFalse(application.contains("config:8888"));
		assertFalse(application.toLowerCase().contains("eureka"));

		Path bootstrap = Paths.get("src/main/resources/bootstrap.yml");
		assertTrue(Files.exists(bootstrap));
		String bootstrapContent = new String(Files.readAllBytes(bootstrap), StandardCharsets.UTF_8);
		assertFalse(bootstrapContent.contains("config:8888"));
		assertFalse(bootstrapContent.contains("fail-fast"));

		Path applicationClass = Paths.get(
				"src/main/java/com/piggymetrics/account/AccountApplication.java");
		assertTrue(Files.exists(applicationClass));
		String appSource = new String(Files.readAllBytes(applicationClass), StandardCharsets.UTF_8);
		assertFalse(appSource.contains("EnableDiscoveryClient"));
		assertFalse(appSource.contains("eureka"));
	}

	@Test
	public void shouldStartImageWithoutEurekaOrConfigServerWhenDockerAvailable() throws Exception {
		assumeTrue("docker CLI must be available", isDockerAvailable());
		assumeTrue("account-service:local image must exist", imageExists("account-service:local"));

		String containerName = "account-service-p3t4-" + System.currentTimeMillis();
		ProcessBuilder pb = new ProcessBuilder(
				"docker", "run", "--rm", "--name", containerName,
				"-e", "MONGODB_HOST=account-mongodb",
				"-e", "MONGODB_PASSWORD=test",
				"-e", "ACCOUNT_SERVICE_PASSWORD=test",
				"-e", "AUTH_SERVICE_URL=http://auth-service:5000",
				"-e", "STATISTICS_SERVICE_URL=http://statistics-service:7000",
				"-e", "RABBITMQ_HOST=rabbitmq",
				"account-service:local"
		);
		pb.redirectErrorStream(true);
		Process process = pb.start();

		StringBuilder output = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
			String line;
			while (System.currentTimeMillis() < deadline && (line = reader.readLine()) != null) {
				output.append(line).append('\n');
				String lower = line.toLowerCase();
				if (lower.contains("started accountapplication")
						|| lower.contains("config:8888")
						|| lower.contains("eureka")
						|| lower.contains("discoveryclient")) {
					break;
				}
			}
		} finally {
			process.destroy();
			process.waitFor(15, TimeUnit.SECONDS);
			if (process.isAlive()) {
				process.destroyForcibly();
			}
			new ProcessBuilder("docker", "rm", "-f", containerName)
					.redirectErrorStream(true).start().waitFor(15, TimeUnit.SECONDS);
		}

		String logs = output.toString();
		assertTrue("SpringApplication for AccountApplication must start:\n" + logs,
				logs.toLowerCase().contains("accountapplication")
						|| logs.toLowerCase().contains("springapplication"));
		assertFalse("must not require http://config:8888:\n" + logs,
				logs.contains("config:8888") || logs.contains("http://config:8888"));
		assertFalse("must not require Eureka registration:\n" + logs,
				logs.toLowerCase().contains("discoveryclient_account")
						|| logs.toLowerCase().contains("registering application account-service with eureka"));
	}

	private static boolean isDockerAvailable() {
		try {
			Process process = new ProcessBuilder("docker", "info").redirectErrorStream(true).start();
			return process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}

	private static boolean imageExists(String image) {
		try {
			Process process = new ProcessBuilder("docker", "image", "inspect", image)
					.redirectErrorStream(true).start();
			return process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0;
		} catch (Exception ex) {
			return false;
		}
	}
}
