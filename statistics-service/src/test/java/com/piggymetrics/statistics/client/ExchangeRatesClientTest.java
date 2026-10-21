package com.piggymetrics.statistics.client;

import com.piggymetrics.statistics.domain.Currency;
import com.piggymetrics.statistics.domain.ExchangeRatesContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
		"spring.cloud.openfeign.circuitbreaker.enabled=true"
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
