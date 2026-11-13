package com.piggymetrics.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.piggymetrics.account.domain.*;
import com.piggymetrics.account.service.AccountService;
import com.sun.security.auth.UserPrincipal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P2.T8 parity: AccountControllerTest covers C1–C4 on account-service
 * with existing validation and response behavior under service-owned settings.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	private AccountController accountController;

	@Mock
	private AccountService accountService;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
	}

	@Test
	public void shouldMapContractsC1ThroughC4() throws Exception {
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
		assertTrue(mappings.containsAll(Arrays.asList(
				"GET /{name}",
				"GET /current",
				"PUT /current",
				"POST /"
		)));
	}

	@Test
	public void shouldGetAccountByName() throws Exception {
		// C1 GET /{name}

		final Account account = new Account();
		account.setName("test");

		when(accountService.findByName(account.getName())).thenReturn(account);

		mockMvc.perform(get("/" + account.getName()))
				.andExpect(jsonPath("$.name").value(account.getName()))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldGetCurrentAccount() throws Exception {
		// C2 GET /current

		final Account account = new Account();
		account.setName("test");

		when(accountService.findByName(account.getName())).thenReturn(account);

		mockMvc.perform(get("/current").principal(new UserPrincipal(account.getName())))
				.andExpect(jsonPath("$.name").value(account.getName()))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldSaveCurrentAccount() throws Exception {
		// C3 PUT /current

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1500));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		Item grocery = new Item();
		grocery.setTitle("Grocery");
		grocery.setAmount(new BigDecimal(10));
		grocery.setCurrency(Currency.USD);
		grocery.setPeriod(TimePeriod.DAY);
		grocery.setIcon("meal");

		Item salary = new Item();
		salary.setTitle("Salary");
		salary.setAmount(new BigDecimal(9100));
		salary.setCurrency(Currency.USD);
		salary.setPeriod(TimePeriod.MONTH);
		salary.setIcon("wallet");

		final Account account = new Account();
		account.setName("test");
		account.setNote("test note");
		account.setLastSeen(new Date());
		account.setSaving(saving);
		account.setExpenses(ImmutableList.of(grocery));
		account.setIncomes(ImmutableList.of(salary));

		String json = mapper.writeValueAsString(account);

		mockMvc.perform(put("/current").principal(new UserPrincipal(account.getName())).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldFailOnValidationTryingToSaveCurrentAccount() throws Exception {

		final Account account = new Account();
		account.setName("test");

		String json = mapper.writeValueAsString(account);

		mockMvc.perform(put("/current").principal(new UserPrincipal(account.getName())).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void shouldRegisterNewAccount() throws Exception {
		// C4 POST /

		final User user = new User();
		user.setUsername("test");
		user.setPassword("password");

		String json = mapper.writeValueAsString(user);
		System.out.println(json);
		mockMvc.perform(post("/").principal(new UserPrincipal("test")).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldFailOnValidationTryingToRegisterNewAccount() throws Exception {

		final User user = new User();
		user.setUsername("t");

		String json = mapper.writeValueAsString(user);

		mockMvc.perform(post("/").principal(new UserPrincipal("test")).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest());
	}
}
