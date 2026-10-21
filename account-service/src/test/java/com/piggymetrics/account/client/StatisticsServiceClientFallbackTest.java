package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(properties = {
        "spring.cloud.openfeign.circuitbreaker.enabled=true"
})
public class StatisticsServiceClientFallbackTest {
    @Autowired
    private StatisticsServiceClient statisticsServiceClient;

    @BeforeEach
    public void setup(CapturedOutput outputCapture) {
        // CapturedOutput resets per test via extension
    }

    @Test
    public void testUpdateStatisticsWithFailFallback(CapturedOutput outputCapture){
        statisticsServiceClient.updateStatistics("test", new Account());
        assertThat(outputCapture.toString(), containsString("Error during update statistics for account: test"));
    }

}
