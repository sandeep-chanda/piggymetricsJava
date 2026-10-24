---
name: P1 account-service settlement observability
overview: "Extend piggymetricsJava account-service with p95 settlement latency, failed-batch counter with retry, GET /observability/dashboard, and tests P1.T1-T11. Letter-to-letter pins; gate is mvn test."
todos:
  - id: step-001
    content: "STEP-001 P1.T6 account.settlement config + SettlementProperties"
    status: pending
  - id: step-002
    content: "STEP-002 Add micrometer-core coordinate to account-service pom"
    status: pending
  - id: step-003
    content: "STEP-003 SettlementObservability p95 timer + SettlementDashboard"
    status: pending
  - id: step-004
    content: "STEP-004 P1.T2 Extend Account document settlement fields"
    status: pending
  - id: step-005
    content: "STEP-005 P1.T3 Repository findByFailedBatchCountGreaterThan"
    status: pending
  - id: step-006
    content: "STEP-006 P1.T4 AuthServiceClient propagate (no fallback)"
    status: pending
  - id: step-007
    content: "STEP-007 P1.T5 Fallback mark + saveChanges retry/latency"
    status: pending
  - id: step-008
    content: "STEP-008 P1.T1 GET /observability/dashboard"
    status: pending
  - id: step-009
    content: "STEP-009 P1.T7 Extend AccountServiceApplicationTests"
    status: pending
  - id: step-010
    content: "STEP-010 P1.T8 Extend StatisticsServiceClientFallbackTest"
    status: pending
  - id: step-011
    content: "STEP-011 P1.T9 Extend AccountControllerTest"
    status: pending
  - id: step-012
    content: "STEP-012 P1.T10 Extend AccountRepositoryTest"
    status: pending
  - id: step-013
    content: "STEP-013 P1.T11 Extend AccountServiceTest"
    status: pending
  - id: step-014
    content: "STEP-014 Gate: mvn test (full suite)"
    status: pending
isProject: false
---
# P1 — account-service settlement observability

## Objective

Implement walk-e2e-20261024-scenario plan P1 on piggymetricsJava: expose a p95 settlement latency metric, add a failed-batch counter with retry on statistics settlement, add a dashboard endpoint that reads both, and extend the named account-service tests. Preserve Feign, Hystrix, OAuth, Eureka. Keep Java 1.8 and Dockerfile FROM java:8-jre.

## Decisions

- **D-001:** Land all four Goal items in account-service (not monitoring).
- **D-002:** Retry keys live under `account.settlement.retry` in shared `account-service.yml`.
- **D-003:** Micrometer Timer/Counter; pin `io.micrometer:micrometer-core` in `account-service/pom.xml`.
- **D-004:** Auth POST `/uaa/users` propagates; statistics PUT uses `StatisticsServiceClientFallback` that marks failed-batch; `AccountServiceImpl` retries.
- **D-005:** Acceptance gate is `mvn test` (not `test-compile`).

## Constraint

- Do not invent production code outside the pinned files.
- Do not ban `account-service/pom.xml` or `AccountServiceImpl.java` from needed edits; JVM major stays 1.8 so Dockerfiles stay `java:8-jre`.
- Gateway routes `/accounts/**`, `/uaa/**`, `/statistics/**` stay as indexed.

## STEP-001 — P1.T6 Config keys + SettlementProperties

Depends on: (none)

Files:
- `config/src/main/resources/shared/account-service.yml`
- `account-service/src/main/java/com/piggymetrics/account/config/SettlementProperties.java`

Do not:
- Change gateway.yml /accounts/** routes
- Raise java.version
- Remove feign.hystrix.enabled

exact_snippet (`config/src/main/resources/shared/account-service.yml`):

~~~yaml
security:
  oauth2:
    client:
      clientId: account-service
      clientSecret: ${ACCOUNT_SERVICE_PASSWORD}
      accessTokenUri: http://auth-service:5000/uaa/oauth/token
      grant-type: client_credentials
      scope: server

spring:
  data:
    mongodb:
      host: account-mongodb
      username: user
      password: ${MONGODB_PASSWORD}
      database: piggymetrics
      port: 27017

server:
  servlet:
    context-path: /accounts
  port: 6000

feign:
  hystrix:
    enabled: true

account:
  settlement:
    retry:
      max-attempts: 3
      backoff-ms: 100
~~~

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/config/SettlementProperties.java`):

~~~java
package com.piggymetrics.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "account.settlement")
public class SettlementProperties {

	private final Retry retry = new Retry();

	public Retry getRetry() {
		return retry;
	}

	public static class Retry {

		private int maxAttempts = 3;

		private long backoffMs = 100L;

		public int getMaxAttempts() {
			return maxAttempts;
		}

		public void setMaxAttempts(int maxAttempts) {
			this.maxAttempts = maxAttempts;
		}

		public long getBackoffMs() {
			return backoffMs;
		}

		public void setBackoffMs(long backoffMs) {
			this.backoffMs = backoffMs;
		}
	}
}
~~~

exact_commands:

~~~bash
grep -n 'account.settlement' config/src/main/resources/shared/account-service.yml
~~~


## STEP-002 — micrometer-core coordinate

Depends on: STEP-001

Files:
- `account-service/pom.xml`

Do not:
- Change java.version away from 1.8
- Replace spring-boot-starter-parent 2.0.3.RELEASE

exact_snippet (`account-service/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>account-service</artifactId>
	<version>1.0-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>account-service</name>

	<parent>
		<groupId>com.piggymetrics</groupId>
		<artifactId>piggymetrics</artifactId>
		<version>1.0-SNAPSHOT</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-oauth2</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-openfeign</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-sleuth</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-core</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>de.flapdoodle.embed</groupId>
			<artifactId>de.flapdoodle.embed.mongo</artifactId>
			<version>1.50.3</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.jayway.jsonpath</groupId>
			<artifactId>json-path</artifactId>
			<version>2.2.0</version>
			<scope>test</scope>
		</dependency>
	</dependencies>
	
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<finalName>account-service</finalName>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.jacoco</groupId>
				<artifactId>jacoco-maven-plugin</artifactId>
				<version>0.7.6.201602180812</version>
				<executions>
					<execution>
						<goals>
							<goal>prepare-agent</goal>
						</goals>
					</execution>
					<execution>
						<id>report</id>
						<phase>test</phase>
						<goals>
							<goal>report</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>
~~~

exact_commands:

~~~bash
grep -n micrometer-core account-service/pom.xml
~~~


## STEP-003 — SettlementObservability + SettlementDashboard

Depends on: STEP-002

Files:
- `account-service/src/main/java/com/piggymetrics/account/service/SettlementObservability.java`
- `account-service/src/main/java/com/piggymetrics/account/domain/SettlementDashboard.java`

Do not:
- Put metrics only in monitoring module
- Use Dropwizard MetricsRegistry

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/service/SettlementObservability.java`):

~~~java
package com.piggymetrics.account.service;

import com.piggymetrics.account.domain.SettlementDashboard;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SettlementObservability {

	public static final String TIMER_NAME = "account.settlement.latency";
	public static final String COUNTER_NAME = "account.settlement.failed.batch";

	private final Timer settlementLatency;
	private final Counter failedBatchCounter;
	private final AtomicBoolean lastAttemptFailed = new AtomicBoolean(false);

	public SettlementObservability(MeterRegistry registry) {
		this.settlementLatency = Timer.builder(TIMER_NAME)
				.publishPercentiles(0.95)
				.publishPercentileHistogram()
				.register(registry);
		this.failedBatchCounter = Counter.builder(COUNTER_NAME).register(registry);
	}

	public void markAttemptFailed() {
		lastAttemptFailed.set(true);
		failedBatchCounter.increment();
	}

	public boolean consumeFailedFlag() {
		return lastAttemptFailed.getAndSet(false);
	}

	public void recordLatencyNanos(long nanos) {
		settlementLatency.record(nanos, TimeUnit.NANOSECONDS);
	}

	public SettlementDashboard snapshot() {
		SettlementDashboard dashboard = new SettlementDashboard();
		dashboard.setSettlementLatencyP95Ms(readP95Millis());
		dashboard.setFailedBatchCount((long) failedBatchCounter.count());
		return dashboard;
	}

	private double readP95Millis() {
		HistogramSnapshot snap = settlementLatency.takeSnapshot();
		for (ValueAtPercentile vap : snap.percentileValues()) {
			if (Math.abs(vap.percentile() - 0.95) < 0.001) {
				return vap.value(TimeUnit.MILLISECONDS);
			}
		}
		return settlementLatency.mean(TimeUnit.MILLISECONDS);
	}
}
~~~

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/domain/SettlementDashboard.java`):

~~~java
package com.piggymetrics.account.domain;

public class SettlementDashboard {

	private double settlementLatencyP95Ms;

	private long failedBatchCount;

	public double getSettlementLatencyP95Ms() {
		return settlementLatencyP95Ms;
	}

	public void setSettlementLatencyP95Ms(double settlementLatencyP95Ms) {
		this.settlementLatencyP95Ms = settlementLatencyP95Ms;
	}

	public long getFailedBatchCount() {
		return failedBatchCount;
	}

	public void setFailedBatchCount(long failedBatchCount) {
		this.failedBatchCount = failedBatchCount;
	}
}
~~~

exact_commands:

~~~bash
grep -n 'publishPercentiles(0.95)' account-service/src/main/java/com/piggymetrics/account/service/SettlementObservability.java
~~~


## STEP-004 — P1.T2 Account document fields

Depends on: STEP-003

Files:
- `account-service/src/main/java/com/piggymetrics/account/domain/Account.java`

Do not:
- Rename collection accounts
- Remove JsonIgnoreProperties
- Change @Id name semantics

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/domain/Account.java`):

~~~java
package com.piggymetrics.account.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Document(collection = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

	@Id
	private String name;

	private Date lastSeen;

	@Valid
	private List<Item> incomes;

	@Valid
	private List<Item> expenses;

	@Valid
	@NotNull
	private Saving saving;

	@Length(min = 0, max = 20_000)
	private String note;

	private Long lastSettlementLatencyMs;

	private long failedBatchCount;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<Item> getIncomes() {
		return incomes;
	}

	public void setIncomes(List<Item> incomes) {
		this.incomes = incomes;
	}

	public List<Item> getExpenses() {
		return expenses;
	}

	public void setExpenses(List<Item> expenses) {
		this.expenses = expenses;
	}

	public Saving getSaving() {
		return saving;
	}

	public void setSaving(Saving saving) {
		this.saving = saving;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getLastSettlementLatencyMs() {
		return lastSettlementLatencyMs;
	}

	public void setLastSettlementLatencyMs(Long lastSettlementLatencyMs) {
		this.lastSettlementLatencyMs = lastSettlementLatencyMs;
	}

	public long getFailedBatchCount() {
		return failedBatchCount;
	}

	public void setFailedBatchCount(long failedBatchCount) {
		this.failedBatchCount = failedBatchCount;
	}
}
~~~

exact_commands:

~~~bash
grep -n 'failedBatchCount' account-service/src/main/java/com/piggymetrics/account/domain/Account.java
~~~


## STEP-005 — P1.T3 Repository query

Depends on: STEP-004

Files:
- `account-service/src/main/java/com/piggymetrics/account/repository/AccountRepository.java`

Do not:
- Delete findByName
- Replace CrudRepository with MongoTemplate-only DAO

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/repository/AccountRepository.java`):

~~~java
package com.piggymetrics.account.repository;

import com.piggymetrics.account.domain.Account;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends CrudRepository<Account, String> {

	Account findByName(String name);

	List<Account> findByFailedBatchCountGreaterThan(long threshold);

}
~~~

exact_commands:

~~~bash
grep -n findByFailedBatchCountGreaterThan account-service/src/main/java/com/piggymetrics/account/repository/AccountRepository.java
~~~


## STEP-006 — P1.T4 AuthServiceClient propagate

Depends on: STEP-005

Files:
- `account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java`

Do not:
- Add fallback= to AuthServiceClient
- Catch authClient.createUser in create()
- Change path away from /uaa/users

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java`):

~~~java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

	@RequestMapping(method = RequestMethod.POST, value = "/uaa/users", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	void createUser(User user);

}
~~~

exact_commands:

~~~bash
grep -n FeignClient account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java
~~~


## STEP-007 — P1.T5 Fallback + retry latency wiring

Depends on: STEP-006 + STEP-003

Files:
- `account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java`
- `account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClientFallback.java`
- `account-service/src/main/java/com/piggymetrics/account/service/AccountService.java`
- `account-service/src/main/java/com/piggymetrics/account/service/AccountServiceImpl.java`

Do not:
- Remove statistics fallback
- Add auth fallback
- Lower gate to test-compile or -DskipTests

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java`):

~~~java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "statistics-service", fallback = StatisticsServiceClientFallback.class)
public interface StatisticsServiceClient {

	@RequestMapping(method = RequestMethod.PUT, value = "/statistics/{accountName}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	void updateStatistics(@PathVariable("accountName") String accountName, Account account);

}
~~~

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClientFallback.java`):

~~~java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.service.SettlementObservability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cdov
 */
@Component
public class StatisticsServiceClientFallback implements StatisticsServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceClientFallback.class);

	@Autowired
	private SettlementObservability settlementObservability;

	@Override
	public void updateStatistics(String accountName, Account account) {
		LOGGER.error("Error during update statistics for account: {}", accountName);
		settlementObservability.markAttemptFailed();
	}
}
~~~

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/service/AccountService.java`):

~~~java
package com.piggymetrics.account.service;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.SettlementDashboard;
import com.piggymetrics.account.domain.User;

public interface AccountService {

	/**
	 * Finds account by given name
	 *
	 * @param accountName
	 * @return found account
	 */
	Account findByName(String accountName);

	/**
	 * Checks if account with the same name already exists
	 * Invokes Auth Service user creation
	 * Creates new account with default parameters
	 *
	 * @param user
	 * @return created account
	 */
	Account create(User user);

	/**
	 * Validates and applies incoming account updates
	 * Invokes Statistics Service update
	 *
	 * @param name
	 * @param update
	 */
	void saveChanges(String name, Account update);

	/**
	 * Returns p95 settlement latency and failed-batch counter snapshot.
	 *
	 * @return settlement dashboard metrics
	 */
	SettlementDashboard getSettlementDashboard();
}
~~~

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/service/AccountServiceImpl.java`):

~~~java
package com.piggymetrics.account.service;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.config.SettlementProperties;
import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Saving;
import com.piggymetrics.account.domain.SettlementDashboard;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class AccountServiceImpl implements AccountService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private StatisticsServiceClient statisticsClient;

	@Autowired
	private AuthServiceClient authClient;

	@Autowired
	private AccountRepository repository;

	@Autowired
	private SettlementObservability settlementObservability;

	@Autowired
	private SettlementProperties settlementProperties;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Account findByName(String accountName) {
		Assert.hasLength(accountName);
		return repository.findByName(accountName);
	}

	/**
	 * {@inheritDoc}
	 * Auth failures from POST /uaa/users propagate to the caller (no Feign fallback).
	 */
	@Override
	public Account create(User user) {

		Account existing = repository.findByName(user.getUsername());
		Assert.isNull(existing, "account already exists: " + user.getUsername());

		authClient.createUser(user);

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(0));
		saving.setCurrency(Currency.getDefault());
		saving.setInterest(new BigDecimal(0));
		saving.setDeposit(false);
		saving.setCapitalization(false);

		Account account = new Account();
		account.setName(user.getUsername());
		account.setLastSeen(new Date());
		account.setSaving(saving);
		account.setFailedBatchCount(0L);

		repository.save(account);

		log.info("new account has been created: " + account.getName());

		return account;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveChanges(String name, Account update) {

		Account account = repository.findByName(name);
		Assert.notNull(account, "can't find account with name " + name);

		account.setIncomes(update.getIncomes());
		account.setExpenses(update.getExpenses());
		account.setSaving(update.getSaving());
		account.setNote(update.getNote());
		account.setLastSeen(new Date());

		long started = System.nanoTime();
		int maxAttempts = settlementProperties.getRetry().getMaxAttempts();
		long backoffMs = settlementProperties.getRetry().getBackoffMs();
		boolean settled = false;
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			statisticsClient.updateStatistics(name, account);
			if (!settlementObservability.consumeFailedFlag()) {
				settled = true;
				break;
			}
			if (attempt < maxAttempts && backoffMs > 0L) {
				try {
					Thread.sleep(backoffMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		long elapsed = System.nanoTime() - started;
		settlementObservability.recordLatencyNanos(elapsed);
		account.setLastSettlementLatencyMs(elapsed / 1_000_000L);
		if (!settled) {
			account.setFailedBatchCount(account.getFailedBatchCount() + 1L);
		}

		repository.save(account);

		log.debug("account {} changes has been saved", name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SettlementDashboard getSettlementDashboard() {
		return settlementObservability.snapshot();
	}
}
~~~

exact_commands:

~~~bash
grep -n 'recordLatencyNanos' account-service/src/main/java/com/piggymetrics/account/service/AccountServiceImpl.java
~~~


## STEP-008 — P1.T1 Dashboard endpoint

Depends on: STEP-007

Files:
- `account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java`

Do not:
- Change context-path away from /accounts
- Remove PreAuthorize on getAccountByName

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java`):

~~~java
package com.piggymetrics.account.controller;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.SettlementDashboard;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class AccountController {

	@Autowired
	private AccountService accountService;

	@PreAuthorize("#oauth2.hasScope('server') or #name.equals('demo')")
	@RequestMapping(path = "/{name}", method = RequestMethod.GET)
	public Account getAccountByName(@PathVariable String name) {
		return accountService.findByName(name);
	}

	@RequestMapping(path = "/current", method = RequestMethod.GET)
	public Account getCurrentAccount(Principal principal) {
		return accountService.findByName(principal.getName());
	}

	@RequestMapping(path = "/current", method = RequestMethod.PUT)
	public void saveCurrentAccount(Principal principal, @Valid @RequestBody Account account) {
		accountService.saveChanges(principal.getName(), account);
	}

	@RequestMapping(path = "/", method = RequestMethod.POST)
	public Account createNewAccount(@Valid @RequestBody User user) {
		return accountService.create(user);
	}

	@RequestMapping(path = "/observability/dashboard", method = RequestMethod.GET)
	public SettlementDashboard getSettlementDashboard() {
		return accountService.getSettlementDashboard();
	}
}
~~~

exact_commands:

~~~bash
grep -n 'observability/dashboard' account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java
~~~


## STEP-009 — P1.T7 AccountServiceApplicationTests

Depends on: STEP-008

Files:
- `account-service/src/test/java/com/piggymetrics/account/AccountServiceApplicationTests.java`

Do not:
- Delete contextLoads

exact_snippet (`account-service/src/test/java/com/piggymetrics/account/AccountServiceApplicationTests.java`):

~~~java
package com.piggymetrics.account;

import com.piggymetrics.account.domain.SettlementDashboard;
import com.piggymetrics.account.service.AccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceApplicationTests {

	@Autowired
	private AccountService accountService;

	@Test
	public void contextLoads() {

	}

	@Test
	public void settlementDashboardBeanWired() {
		SettlementDashboard dashboard = accountService.getSettlementDashboard();
		assertNotNull(dashboard);
		assertTrue(dashboard.getFailedBatchCount() >= 0L);
		assertTrue(dashboard.getSettlementLatencyP95Ms() >= 0.0d);
	}

}
~~~

exact_commands:

~~~bash
grep -n settlementDashboardBeanWired account-service/src/test/java/com/piggymetrics/account/AccountServiceApplicationTests.java
~~~


## STEP-010 — P1.T8 StatisticsServiceClientFallbackTest

Depends on: STEP-007

Files:
- `account-service/src/test/java/com/piggymetrics/account/client/StatisticsServiceClientFallbackTest.java`

Do not:
- Remove feign.hystrix.enabled=true test property

exact_snippet (`account-service/src/test/java/com/piggymetrics/account/client/StatisticsServiceClientFallbackTest.java`):

~~~java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.service.SettlementObservability;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
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

	@Autowired
	private SettlementObservability settlementObservability;

	@Rule
	public final OutputCapture outputCapture = new OutputCapture();

	@Before
	public void setup() {
		outputCapture.reset();
	}

	@Test
	public void testUpdateStatisticsWithFailFallback(){
		long before = settlementObservability.snapshot().getFailedBatchCount();
		statisticsServiceClient.updateStatistics("test", new Account());

		outputCapture.expect(containsString("Error during update statistics for account: test"));
		assertTrue(settlementObservability.consumeFailedFlag());
		assertTrue(settlementObservability.snapshot().getFailedBatchCount() > before);
	}

}
~~~

exact_commands:

~~~bash
grep -n consumeFailedFlag account-service/src/test/java/com/piggymetrics/account/client/StatisticsServiceClientFallbackTest.java
~~~


## STEP-011 — P1.T9 AccountControllerTest

Depends on: STEP-008

Files:
- `account-service/src/test/java/com/piggymetrics/account/controller/AccountControllerTest.java`

Do not:
- Remove validation failure tests

exact_snippet (`account-service/src/test/java/com/piggymetrics/account/controller/AccountControllerTest.java`):

~~~java
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

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
	public void shouldGetAccountByName() throws Exception {

		final Account account = new Account();
		account.setName("test");

		when(accountService.findByName(account.getName())).thenReturn(account);

		mockMvc.perform(get("/" + account.getName()))
				.andExpect(jsonPath("$.name").value(account.getName()))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldGetCurrentAccount() throws Exception {

		final Account account = new Account();
		account.setName("test");

		when(accountService.findByName(account.getName())).thenReturn(account);

		mockMvc.perform(get("/current").principal(new UserPrincipal(account.getName())))
				.andExpect(jsonPath("$.name").value(account.getName()))
				.andExpect(status().isOk());
	}

	@Test
	public void shouldSaveCurrentAccount() throws Exception {

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

	@Test
	public void shouldReturnSettlementDashboard() throws Exception {

		SettlementDashboard dashboard = new SettlementDashboard();
		dashboard.setSettlementLatencyP95Ms(12.5d);
		dashboard.setFailedBatchCount(3L);

		when(accountService.getSettlementDashboard()).thenReturn(dashboard);

		mockMvc.perform(get("/observability/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.settlementLatencyP95Ms").value(12.5d))
				.andExpect(jsonPath("$.failedBatchCount").value(3));
	}
}
~~~

exact_commands:

~~~bash
grep -n shouldReturnSettlementDashboard account-service/src/test/java/com/piggymetrics/account/controller/AccountControllerTest.java
~~~


## STEP-012 — P1.T10 AccountRepositoryTest

Depends on: STEP-005

Files:
- `account-service/src/test/java/com/piggymetrics/account/repository/AccountRepositoryTest.java`

Do not:
- Remove @DataMongoTest

exact_snippet (`account-service/src/test/java/com/piggymetrics/account/repository/AccountRepositoryTest.java`):

~~~java
package com.piggymetrics.account.repository;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.Currency;
import com.piggymetrics.account.domain.Item;
import com.piggymetrics.account.domain.Saving;
import com.piggymetrics.account.domain.TimePeriod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataMongoTest
public class AccountRepositoryTest {

	@Autowired
	private AccountRepository repository;

	@Test
	public void shouldFindAccountByName() {

		Account stub = getStubAccount();
		repository.save(stub);

		Account found = repository.findByName(stub.getName());
		assertEquals(stub.getLastSeen(), found.getLastSeen());
		assertEquals(stub.getNote(), found.getNote());
		assertEquals(stub.getIncomes().size(), found.getIncomes().size());
		assertEquals(stub.getExpenses().size(), found.getExpenses().size());
		assertEquals(stub.getFailedBatchCount(), found.getFailedBatchCount());
		assertEquals(stub.getLastSettlementLatencyMs(), found.getLastSettlementLatencyMs());
	}

	@Test
	public void shouldFindAccountsWithFailedBatches() {

		Account stub = getStubAccount();
		stub.setFailedBatchCount(2L);
		repository.save(stub);

		List<Account> found = repository.findByFailedBatchCountGreaterThan(0L);
		assertTrue(found.size() >= 1);
		assertEquals("test", found.get(0).getName());
	}

	private Account getStubAccount() {

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1500));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		Item vacation = new Item();
		vacation.setTitle("Vacation");
		vacation.setAmount(new BigDecimal(3400));
		vacation.setCurrency(Currency.EUR);
		vacation.setPeriod(TimePeriod.YEAR);
		vacation.setIcon("tourism");

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

		Account account = new Account();
		account.setName("test");
		account.setNote("test note");
		account.setLastSeen(new Date());
		account.setSaving(saving);
		account.setExpenses(Arrays.asList(grocery, vacation));
		account.setIncomes(Arrays.asList(salary));
		account.setFailedBatchCount(0L);
		account.setLastSettlementLatencyMs(0L);

		return account;
	}
}
~~~

exact_commands:

~~~bash
grep -n shouldFindAccountsWithFailedBatches account-service/src/test/java/com/piggymetrics/account/repository/AccountRepositoryTest.java
~~~


## STEP-013 — P1.T11 AccountServiceTest

Depends on: STEP-007

Files:
- `account-service/src/test/java/com/piggymetrics/account/service/AccountServiceTest.java`

Do not:
- Swallow auth exceptions in create()
- Self-stub accountService.findByName

exact_snippet (`account-service/src/test/java/com/piggymetrics/account/service/AccountServiceTest.java`):

~~~java
package com.piggymetrics.account.service;

import com.piggymetrics.account.client.AuthServiceClient;
import com.piggymetrics.account.client.StatisticsServiceClient;
import com.piggymetrics.account.config.SettlementProperties;
import com.piggymetrics.account.domain.*;
import com.piggymetrics.account.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AccountServiceTest {

	@InjectMocks
	private AccountServiceImpl accountService;

	@Mock
	private StatisticsServiceClient statisticsClient;

	@Mock
	private AuthServiceClient authClient;

	@Mock
	private AccountRepository repository;

	@Mock
	private SettlementObservability settlementObservability;

	@Mock
	private SettlementProperties settlementProperties;

	@Before
	public void setup() {
		initMocks(this);
		SettlementProperties.Retry retry = new SettlementProperties.Retry();
		retry.setMaxAttempts(3);
		retry.setBackoffMs(0L);
		when(settlementProperties.getRetry()).thenReturn(retry);
		when(settlementObservability.consumeFailedFlag()).thenReturn(false);
		SettlementDashboard dashboard = new SettlementDashboard();
		dashboard.setSettlementLatencyP95Ms(0.0d);
		dashboard.setFailedBatchCount(0L);
		when(settlementObservability.snapshot()).thenReturn(dashboard);
	}

	@Test
	public void shouldFindByName() {

		final Account account = new Account();
		account.setName("test");

		when(repository.findByName(account.getName())).thenReturn(account);
		Account found = accountService.findByName(account.getName());

		assertEquals(account, found);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNameIsEmpty() {
		accountService.findByName("");
	}

	@Test
	public void shouldCreateAccountWithGivenUser() {

		User user = new User();
		user.setUsername("test");

		Account account = accountService.create(user);

		assertEquals(user.getUsername(), account.getName());
		assertEquals(0, account.getSaving().getAmount().intValue());
		assertEquals(Currency.getDefault(), account.getSaving().getCurrency());
		assertEquals(0, account.getSaving().getInterest().intValue());
		assertEquals(false, account.getSaving().getDeposit());
		assertEquals(false, account.getSaving().getCapitalization());
		assertNotNull(account.getLastSeen());
		assertEquals(0L, account.getFailedBatchCount());

		verify(authClient, times(1)).createUser(user);
		verify(repository, times(1)).save(account);
	}

	@Test(expected = RuntimeException.class)
	public void shouldPropagateAuthServiceFailureOnCreate() {
		User user = new User();
		user.setUsername("test");
		doThrow(new RuntimeException("auth down")).when(authClient).createUser(user);
		accountService.create(user);
	}

	@Test
	public void shouldSaveChangesWhenUpdatedAccountGiven() {

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

		Saving saving = new Saving();
		saving.setAmount(new BigDecimal(1500));
		saving.setCurrency(Currency.USD);
		saving.setInterest(new BigDecimal("3.32"));
		saving.setDeposit(true);
		saving.setCapitalization(false);

		final Account update = new Account();
		update.setName("test");
		update.setNote("test note");
		update.setIncomes(Arrays.asList(salary));
		update.setExpenses(Arrays.asList(grocery));
		update.setSaving(saving);

		final Account account = new Account();

		when(repository.findByName("test")).thenReturn(account);
		accountService.saveChanges("test", update);

		assertEquals(update.getNote(), account.getNote());
		assertNotNull(account.getLastSeen());
		assertNotNull(account.getLastSettlementLatencyMs());

		assertEquals(update.getSaving().getAmount(), account.getSaving().getAmount());
		assertEquals(update.getSaving().getCurrency(), account.getSaving().getCurrency());
		assertEquals(update.getSaving().getInterest(), account.getSaving().getInterest());
		assertEquals(update.getSaving().getDeposit(), account.getSaving().getDeposit());
		assertEquals(update.getSaving().getCapitalization(), account.getSaving().getCapitalization());

		assertEquals(update.getExpenses().size(), account.getExpenses().size());
		assertEquals(update.getIncomes().size(), account.getIncomes().size());

		assertEquals(update.getExpenses().get(0).getTitle(), account.getExpenses().get(0).getTitle());
		assertEquals(0, update.getExpenses().get(0).getAmount().compareTo(account.getExpenses().get(0).getAmount()));
		assertEquals(update.getExpenses().get(0).getCurrency(), account.getExpenses().get(0).getCurrency());
		assertEquals(update.getExpenses().get(0).getPeriod(), account.getExpenses().get(0).getPeriod());
		assertEquals(update.getExpenses().get(0).getIcon(), account.getExpenses().get(0).getIcon());

		assertEquals(update.getIncomes().get(0).getTitle(), account.getIncomes().get(0).getTitle());
		assertEquals(0, update.getIncomes().get(0).getAmount().compareTo(account.getIncomes().get(0).getAmount()));
		assertEquals(update.getIncomes().get(0).getCurrency(), account.getIncomes().get(0).getCurrency());
		assertEquals(update.getIncomes().get(0).getPeriod(), account.getIncomes().get(0).getPeriod());
		assertEquals(update.getIncomes().get(0).getIcon(), account.getIncomes().get(0).getIcon());

		verify(repository, times(1)).save(account);
		verify(statisticsClient, times(1)).updateStatistics("test", account);
		verify(settlementObservability, times(1)).recordLatencyNanos(anyLong());
	}

	@Test
	public void shouldRetryStatisticsUpdateAndCountFailedBatch() {
		final Account update = new Account();
		update.setNote("n");
		update.setIncomes(Arrays.asList());
		update.setExpenses(Arrays.asList());
		Saving saving = new Saving();
		saving.setAmount(BigDecimal.ZERO);
		saving.setCurrency(Currency.USD);
		saving.setInterest(BigDecimal.ZERO);
		saving.setDeposit(false);
		saving.setCapitalization(false);
		update.setSaving(saving);

		final Account account = new Account();
		account.setFailedBatchCount(0L);
		when(repository.findByName("test")).thenReturn(account);
		when(settlementObservability.consumeFailedFlag()).thenReturn(true, true, true);

		accountService.saveChanges("test", update);

		verify(statisticsClient, times(3)).updateStatistics(eq("test"), any(Account.class));
		assertEquals(1L, account.getFailedBatchCount());
		verify(settlementObservability, times(1)).recordLatencyNanos(anyLong());
	}

	@Test
	public void shouldExposeSettlementDashboard() {
		SettlementDashboard dashboard = accountService.getSettlementDashboard();
		assertNotNull(dashboard);
		assertTrue(dashboard.getFailedBatchCount() >= 0L);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNoAccountsExistedWithGivenName() {
		final Account update = new Account();
		update.setIncomes(Arrays.asList(new Item()));
		update.setExpenses(Arrays.asList(new Item()));

		when(repository.findByName("test")).thenReturn(null);
		accountService.saveChanges("test", update);
	}
}
~~~

exact_commands:

~~~bash
grep -n shouldRetryStatisticsUpdateAndCountFailedBatch account-service/src/test/java/com/piggymetrics/account/service/AccountServiceTest.java
~~~


## STEP-014 — Gate

Depends on: STEP-009 through STEP-013

After all pins are applied, run the repository suite:

exact_commands:

~~~bash
mvn test
~~~

Acceptance: exit code 0. Do not use `mvn test-compile` or `-DskipTests`.

## Out of scope

- Production edits to auth-service, gateway, monitoring, notification-service, registry, turbine-stream-service
- statistics-service production code (P1 lands observability in account-service)
- JVM major upgrade

## iDAG

STEP-001 → STEP-002 → STEP-003 → STEP-004 → STEP-005 → STEP-006 → STEP-007 → STEP-008 → tests (009–013) → STEP-014
