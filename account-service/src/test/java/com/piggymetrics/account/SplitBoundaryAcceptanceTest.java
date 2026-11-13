package com.piggymetrics.account;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClientFallback;
import com.piggymetrics.account.controller.AccountController;
import com.piggymetrics.account.domain.Account;
import org.junit.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * P4 split-boundary acceptance: record C2 as future move; keep C1/C3/C4,
 * accounts ownership, Feign edges, and retired config on account-service.
 */
public class SplitBoundaryAcceptanceTest {

	@Test
	public void shouldRetainC1C3C4AndKeepC2UntilOutOfScopeCutover() throws Exception {
		Set<String> mappings = new HashSet<String>();
		for (Method method : AccountController.class.getDeclaredMethods()) {
			RequestMapping mapping = method.getAnnotation(RequestMapping.class);
			if (mapping == null) {
				continue;
			}
			String path = mapping.path().length > 0 ? mapping.path()[0] : "";
			RequestMethod http = mapping.method().length > 0 ? mapping.method()[0] : null;
			mappings.add(http + " " + path);
		}

		assertTrue("C1 GET /{name} must stay on account-service",
				mappings.contains("GET /{name}"));
		assertTrue("C2 GET /current remains on account-service until out-of-scope cutover",
				mappings.contains("GET /current"));
		assertTrue("C3 PUT /current must stay on account-service",
				mappings.contains("PUT /current"));
		assertTrue("C4 POST / must stay on account-service",
				mappings.contains("POST /"));
		assertTrue(mappings.containsAll(Arrays.asList(
				"GET /{name}",
				"GET /current",
				"PUT /current",
				"POST /"
		)));

		Path controller = Paths.get(
				"src/main/java/com/piggymetrics/account/controller/AccountController.java");
		assertTrue(Files.exists(controller));
		assertFalse("account-query-service must not exist in this scoped delivery",
				Files.exists(Paths.get("..").resolve("account-query-service")));
	}

	@Test
	public void shouldKeepAccountsCollectionOwnedByAccountService() throws Exception {
		Document document = Account.class.getAnnotation(Document.class);
		assertNotNull("Account must remain a Mongo @Document", document);
		assertEquals("accounts", document.collection());
		assertEquals("com.piggymetrics.account.domain.Account", Account.class.getName());

		Path accountSource = Paths.get(
				"src/main/java/com/piggymetrics/account/domain/Account.java");
		assertTrue(Files.exists(accountSource));
		String content = new String(Files.readAllBytes(accountSource), StandardCharsets.UTF_8);
		assertTrue(content.contains("@Document(collection = \"accounts\")"));
		assertFalse("Account entity must not move to account-query-service",
				Files.exists(Paths.get("..").resolve("account-query-service")
						.resolve("src/main/java/com/piggymetrics/account/domain/Account.java")));
	}

	@Test
	public void shouldKeepAccountServicePomAsFutureServiceShapeTemplate() throws Exception {
		Path queryService = Paths.get("..").resolve("account-query-service");
		assertFalse("Do not introduce account-query-service in this scoped delivery",
				Files.exists(queryService));

		Path pom = Paths.get("pom.xml");
		assertTrue(Files.exists(pom));
		String pomContent = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
		assertTrue("account-service remains jar packaging shape template",
				pomContent.contains("<packaging>jar</packaging>"));
		assertTrue(pomContent.contains("<artifactId>account-service</artifactId>"));
		assertTrue(pomContent.contains("<finalName>account-service</finalName>"));
		assertTrue("OpenFeign remains for retained edges",
				pomContent.contains("spring-cloud-starter-openfeign"));
		assertTrue("Hystrix remains for statistics fallback",
				pomContent.contains("spring-cloud-starter-netflix-hystrix"));
	}

	@Test
	public void shouldKeepAuthServiceEdgeWithC4CreatePath() throws Exception {
		FeignClient feign = AuthServiceClient.class.getAnnotation(FeignClient.class);
		assertNotNull(feign);
		assertEquals("auth-service", feign.name());
		assertEquals("${auth-service.url}", feign.url());

		Method createUser = AuthServiceClient.class.getMethod("createUser",
				com.piggymetrics.account.domain.User.class);
		RequestMapping mapping = createUser.getAnnotation(RequestMapping.class);
		assertNotNull(mapping);
		assertEquals(RequestMethod.POST, mapping.method()[0]);
		assertEquals("/uaa/users", mapping.value()[0]);

		Path client = Paths.get(
				"src/main/java/com/piggymetrics/account/client/AuthServiceClient.java");
		assertTrue(Files.exists(client));
		assertFalse(Files.exists(Paths.get("..").resolve("account-query-service")
				.resolve("src/main/java/com/piggymetrics/account/client/AuthServiceClient.java")));
	}

	@Test
	public void shouldKeepStatisticsServiceEdgeWithC3UpdatePath() throws Exception {
		FeignClient feign = StatisticsServiceClient.class.getAnnotation(FeignClient.class);
		assertNotNull(feign);
		assertEquals("statistics-service", feign.name());
		assertEquals("${statistics-service.url}", feign.url());
		assertEquals(StatisticsServiceClientFallback.class, feign.fallback());

		Method updateStatistics = StatisticsServiceClient.class.getMethod(
				"updateStatistics", String.class, Account.class);
		RequestMapping mapping = updateStatistics.getAnnotation(RequestMapping.class);
		assertNotNull(mapping);
		assertEquals(RequestMethod.PUT, mapping.method()[0]);
		assertEquals("/statistics/{accountName}", mapping.value()[0]);

		Path client = Paths.get(
				"src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java");
		assertTrue(Files.exists(client));
		assertTrue(Files.exists(Paths.get(
				"src/main/java/com/piggymetrics/account/client/StatisticsServiceClientFallback.java")));
	}

	@Test
	public void shouldKeepConfigEdgeRetiredWithServiceOwnedSettings() throws Exception {
		Path bootstrap = Paths.get("src/main/resources/bootstrap.yml");
		assertTrue(Files.exists(bootstrap));
		String bootstrapContent = new String(Files.readAllBytes(bootstrap), StandardCharsets.UTF_8);
		assertTrue(bootstrapContent.contains("account-service"));
		assertFalse("bootstrap.yml must not point at http://config:8888",
				bootstrapContent.contains("config:8888")
						|| bootstrapContent.contains("http://config:8888"));
		assertFalse("bootstrap.yml must not keep fail-fast",
				bootstrapContent.contains("fail-fast"));
		assertFalse("bootstrap.yml must not keep spring.cloud.config keys",
				bootstrapContent.contains("spring.cloud.config")
						|| (bootstrapContent.contains("cloud:")
						&& bootstrapContent.contains("config:")));

		Path applicationYml = Paths.get("src/main/resources/application.yml");
		assertTrue("service-owned application.yml must serve remaining settings",
				Files.exists(applicationYml));

		Path pom = Paths.get("pom.xml");
		String pomContent = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
		assertFalse("config client starter must stay retired",
				pomContent.contains("spring-cloud-starter-config"));
	}
}
