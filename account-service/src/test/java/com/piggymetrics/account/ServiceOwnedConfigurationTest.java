package com.piggymetrics.account;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * P2.T1–P2.T2: account-service retains local identity without config-server
 * block and without spring-cloud-starter-config on the classpath.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceOwnedConfigurationTest {

	@Autowired
	private Environment environment;

	@Value("${spring.application.name}")
	private String applicationName;

	@Test
	public void shouldRetainApplicationIdentityWithoutConfigServerBlock() throws Exception {
		assertEquals("account-service", applicationName);
		assertFalse(environment.getProperty("spring.cloud.config.enabled", Boolean.class, true));
		assertFalse(environment.getProperty("spring.cloud.config.fail-fast", Boolean.class, false));

		Path bootstrap = Paths.get("src/main/resources/bootstrap.yml");
		assertTrue("bootstrap.yml must exist under account-service", Files.exists(bootstrap));
		String bootstrapContent = new String(Files.readAllBytes(bootstrap), StandardCharsets.UTF_8);
		assertTrue(bootstrapContent.contains("account-service"));
		assertFalse("bootstrap.yml must not point at http://config:8888",
				bootstrapContent.contains("config:8888") || bootstrapContent.contains("http://config:8888"));
		assertFalse("bootstrap.yml must not keep fail-fast against the shared config server",
				bootstrapContent.contains("fail-fast"));
	}

	@Test
	public void shouldNotRequireSpringCloudConfigClientOnClasspath() throws Exception {
		try {
			Class.forName("org.springframework.cloud.config.client.ConfigServicePropertySourceLocator");
			fail("spring-cloud-starter-config must not be on the account-service classpath");
		} catch (ClassNotFoundException expected) {
			// expected after P2.T2 retires the config client dependency
		}

		Path pom = Paths.get("pom.xml");
		assertTrue(Files.exists(pom));
		String pomContent = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
		assertFalse("pom.xml must not declare spring-cloud-starter-config",
				pomContent.contains("spring-cloud-starter-config"));
		assertTrue("OpenFeign starter must remain",
				pomContent.contains("spring-cloud-starter-openfeign"));
		assertTrue("Hystrix starter must remain",
				pomContent.contains("spring-cloud-starter-netflix-hystrix"));
	}
}
