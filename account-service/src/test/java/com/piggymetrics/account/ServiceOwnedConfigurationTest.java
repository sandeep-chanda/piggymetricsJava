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

/**
 * P2.T1: account-service retains local application identity without a
 * mandatory shared config-server block.
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
}
