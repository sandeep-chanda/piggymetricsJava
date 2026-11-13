package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author cdov
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
  "feign.hystrix.enabled=true"
})
public class StatisticsServiceClientFallbackTest {
  @Autowired
  private StatisticsServiceClient statisticsServiceClient;

  @Value("${statistics-service.url}")
  private String statisticsServiceUrl;

  @Value("${auth-service.url}")
  private String authServiceUrl;

  @Rule
  public final OutputCapture outputCapture = new OutputCapture();

  @Before
  public void setup() {
  outputCapture.reset();
  }

  @Test
  public void testUpdateStatisticsWithFailFallback(){
  assertNotNull(statisticsServiceUrl);
  assertNotNull(authServiceUrl);
  assertTrue(statisticsServiceUrl.startsWith("http://"));
  statisticsServiceClient.updateStatistics("test", new Account());

  outputCapture.expect(containsString("Error during update statistics for account: test"));

  }

}
