package com.piggymetrics.account;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * P2 acceptance: account-service starts from service-owned settings without
 * the shared config server.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceOwnedConfigurationTest {

	@Autowired
	private Environment environment;

	@Value("${spring.application.name}")
	private String applicationName;

	@Test
	public void shouldRetainApplicationIdentityWithoutConfigServerBlock() {
		assertEquals("account-service", applicationName);
		assertFalse(environment.getProperty("spring.cloud.config.enabled", Boolean.class, true));
		assertFalse(environment.getProperty("spring.cloud.config.fail-fast", Boolean.class, false));
	}

	@Test
	public void shouldNotRequireSpringCloudConfigClientOnClasspath() {
		try {
			Class.forName("org.springframework.cloud.config.client.ConfigServicePropertySourceLocator");
			fail("spring-cloud-starter-config must not be on the account-service classpath");
		} catch (ClassNotFoundException expected) {
			// expected after P2.T2 retires the config client dependency
		}
	}
}
