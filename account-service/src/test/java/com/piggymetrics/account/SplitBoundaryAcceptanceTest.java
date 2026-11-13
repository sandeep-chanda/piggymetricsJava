package com.piggymetrics.account;

import com.piggymetrics.account.controller.AccountController;
import com.piggymetrics.account.domain.Account;
import org.junit.Test;
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
}
