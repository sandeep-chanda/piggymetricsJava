package com.piggymetrics.statistics.client;

import com.piggymetrics.statistics.domain.Currency;
import com.piggymetrics.statistics.domain.ExchangeRatesContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
		"spring.cloud.openfeign.circuitbreaker.enabled=true",
		"rates.url=http://127.0.0.1:9"
})
public class ExchangeRatesClientTest {

	@Autowired
	private ExchangeRatesClient client;

	@Test
	public void shouldRetrieveExchangeRates() {

		ExchangeRatesContainer container = client.getRates(Currency.getBase());

		assertNotNull(container);
		assertEquals(Currency.getBase(), container.getBase());
		assertNotNull(container.getRates());
		assertTrue(container.getRates().isEmpty());
	}

	@Test
	public void shouldRetrieveExchangeRatesForSpecifiedCurrency() {

		ExchangeRatesContainer container = client.getRates(Currency.getBase());

		assertNotNull(container);
		assertEquals(Currency.getBase(), container.getBase());
		assertNotNull(container.getRates());
	}
}
