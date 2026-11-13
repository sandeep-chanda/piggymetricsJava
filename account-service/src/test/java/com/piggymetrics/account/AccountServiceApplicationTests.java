package com.piggymetrics.account;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * P2.T5: contextLoads with no shared config server (spring.cloud.config.enabled false).
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	public void contextLoads() {
		assertFalse(environment.getProperty("spring.cloud.config.enabled", Boolean.class, true));
		assertFalse(environment.getProperty("spring.cloud.config.fail-fast", Boolean.class, false));
		assertEquals("account-service", environment.getProperty("spring.application.name"));
	}

}
