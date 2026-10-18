---
name: Walk 17 Oct modernization
overview: Upgrade piggymetricsJava in place to Spring Boot 2.3.12.RELEASE and Spring Cloud Hoxton.SR12 on Java 1.8, keeping the current Eureka, Feign, Zuul, OAuth2, Hystrix, Config, and Turbine modules.
todos:
  - id: step-000
    content: STEP-000 Create branch walk-17-oct-modernization from master
    status: pending
  - id: step-001
    content: STEP-001 Set root parent to Boot 2.3.12.RELEASE and Hoxton.SR12
    status: pending
  - id: step-002
    content: STEP-002 Add validation starter on account-service and drop embed pin
    status: pending
  - id: step-003
    content: STEP-003 Add validation starter on auth-service and drop embed pin
    status: pending
  - id: step-004
    content: STEP-004 Add validation starter on statistics-service and drop embed pin
    status: pending
  - id: step-005
    content: STEP-005 Add validation starter on notification-service and drop embed pin
    status: pending
  - id: step-006
    content: STEP-006 Point statistics conversions at MongoCustomConversions
    status: pending
  - id: step-007
    content: STEP-007 Point notification conversions at MongoCustomConversions
    status: pending
  - id: step-008
    content: STEP-008 Run root Maven tests and Compose smoke checks
    status: pending
isProject: true
---

# Walk 17 Oct modernization

## Decisions

The target stack is Spring Boot 2.3.12.RELEASE, Spring Cloud Hoxton.SR12, and Java 1.8 on branch walk-17-oct-modernization cut from master. Eureka, OpenFeign, Zuul, spring-cloud-starter-oauth2, Hystrix, Spring Cloud Config, and Turbine stay on their current artifactIds because Hoxton.SR12 still manages them and the spec forbids a rewrite. Boot 3 and Spring Cloud 2020.0.0 are rejected: that train deletes Zuul, Hystrix, and Turbine from the BOM.

Four modules that import javax.validation gain spring-boot-starter-validation, because Boot 2.3 stopped exporting that API from spring-boot-starter-web. statistics-service and notification-service switch their conversion beans to MongoCustomConversions. The embed-mongo test pin 1.50.3 is removed so Boot manages 2.2.0. Config, gateway, monitoring, registry, and turbine-stream-service keep their current manifests and sources. .travis.yml and both Compose files stay byte-identical.

## Branch

Exact branch: `walk-17-oct-modernization`.

STEP-000 exact_commands:

~~~bash
git checkout master
git checkout -b walk-17-oct-modernization
~~~

## Root parent

STEP-001 writes `pom.xml`. java.version stays 1.8. Do not add modules.

exact_snippet (`pom.xml`):

~~~xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.piggymetrics</groupId>
	<artifactId>piggymetrics</artifactId>
	<version>1.0-SNAPSHOT</version>
	<packaging>pom</packaging>
	<name>piggymetrics</name>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.3.12.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<spring-cloud.version>Hoxton.SR12</spring-cloud.version>
		<java.version>1.8</java.version>
	</properties>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	
	<modules>
		<module>config</module>
		<module>monitoring</module>
		<module>registry</module>
		<module>gateway</module>
		<module>auth-service</module>
		<module>account-service</module>
		<module>statistics-service</module>
		<module>notification-service</module>
		<module>turbine-stream-service</module>
	</modules>

</project>
~~~

exact_commands:

~~~bash
mvn -B -N help:evaluate -Dexpression=project.parent.version -q -DforceStdout
mvn -B -N help:evaluate -Dexpression=spring-cloud.version -q -DforceStdout
mvn -B -N help:evaluate -Dexpression=java.version -q -DforceStdout
~~~

## Account manifest

STEP-002 writes `account-service/pom.xml`. Do not edit account-service Java sources.

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
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
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
mvn -B -pl account-service -am test
~~~

## Auth manifest

STEP-003 writes `auth-service/pom.xml`. Do not edit auth-service Java sources. UserController stays on /users and /users/current.

exact_snippet (`auth-service/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>auth-service</artifactId>
	<version>1.0-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>auth-service</name>

	<parent>
		<groupId>com.piggymetrics</groupId>
		<artifactId>piggymetrics</artifactId>
		<version>1.0-SNAPSHOT</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-oauth2</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
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
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>de.flapdoodle.embed</groupId>
			<artifactId>de.flapdoodle.embed.mongo</artifactId>
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
					<finalName>auth-service</finalName>
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
mvn -B -pl auth-service -am test
~~~

## Statistics manifest

STEP-004 writes `statistics-service/pom.xml`. Guava stays at version 19.0. Full tests run in STEP-006 after the conversion bean edit.

exact_snippet (`statistics-service/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>statistics-service</artifactId>
	<version>1.0-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>statistics-service</name>

	<parent>
		<groupId>com.piggymetrics</groupId>
		<artifactId>piggymetrics</artifactId>
		<version>1.0-SNAPSHOT</version>
	</parent>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-oauth2</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
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
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>com.google.guava</groupId>
			<artifactId>guava</artifactId>
			<version>19.0</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>de.flapdoodle.embed</groupId>
			<artifactId>de.flapdoodle.embed.mongo</artifactId>
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
					<finalName>statistics-service</finalName>
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
mvn -B -pl statistics-service -am test -DskipTests
~~~

## Notification manifest

STEP-005 writes `notification-service/pom.xml`. Module version stays 1.0.0-SNAPSHOT. Full tests run in STEP-007.

exact_snippet (`notification-service/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<artifactId>notification-service</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>notification-service</name>

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
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
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
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-bus-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-netflix-hystrix-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-mail</artifactId>
		</dependency>

		<dependency>
			<groupId>de.flapdoodle.embed</groupId>
			<artifactId>de.flapdoodle.embed.mongo</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>com.jayway.jsonpath</groupId>
			<artifactId>json-path</artifactId>
			<version>2.2.0</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<finalName>notification-service</finalName>
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
mvn -B -pl notification-service -am test -DskipTests
~~~

## Statistics conversions

STEP-006 writes `statistics-service/src/main/java/com/piggymetrics/statistics/StatisticsApplication.java`. Controllers, Feign clients, and converter classes stay byte-identical.

exact_snippet (`statistics-service/src/main/java/com/piggymetrics/statistics/StatisticsApplication.java`):

~~~java
package com.piggymetrics.statistics;

import com.piggymetrics.statistics.repository.converter.DataPointIdReaderConverter;
import com.piggymetrics.statistics.repository.converter.DataPointIdWriterConverter;
import com.piggymetrics.statistics.service.security.CustomUserInfoTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOAuth2Client
@EnableFeignClients
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class StatisticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatisticsApplication.class, args);
	}

	@Configuration
	static class CustomConversionsConfig {

		@Bean
		public MongoCustomConversions customConversions() {
			return new MongoCustomConversions(Arrays.asList(new DataPointIdReaderConverter(),
					new DataPointIdWriterConverter()));
		}
	}
}
~~~

exact_commands:

~~~bash
mvn -B -pl statistics-service -am test
~~~

## Notification conversions

STEP-007 writes `notification-service/src/main/java/com/piggymetrics/notification/NotificationServiceApplication.java`. RecipientController, AccountServiceClient, and the cron methods stay byte-identical.

exact_snippet (`notification-service/src/main/java/com/piggymetrics/notification/NotificationServiceApplication.java`):

~~~java
package com.piggymetrics.notification;

import com.piggymetrics.notification.repository.converter.FrequencyReaderConverter;
import com.piggymetrics.notification.repository.converter.FrequencyWriterConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@EnableOAuth2Client
@EnableFeignClients
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableScheduling
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Configuration
	static class CustomConversionsConfig {

		@Bean
		public MongoCustomConversions customConversions() {
			return new MongoCustomConversions(Arrays.asList(new FrequencyReaderConverter(),
					new FrequencyWriterConverter()));
		}
	}
}
~~~

exact_commands:

~~~bash
mvn -B -pl notification-service -am test
~~~

## Verify

STEP-008 runs from the repository root. Do not edit .travis.yml, the Compose files, or gateway.yml. Do not start the production compose file in the same session, because it pulls sqshq images onto the same host ports. Validate that file with docker-compose config, then start the README dev overlay.

exact_commands:

~~~bash
mvn -B test
git diff --exit-code -- .travis.yml
git diff --exit-code -- docker-compose.yml docker-compose.dev.yml
git diff --exit-code -- config/src/main/resources/shared/gateway.yml
mvn -B package -DskipTests
docker-compose -f docker-compose.yml config
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
curl -fsS http://127.0.0.1:8888/actuator/health
curl -fsS http://127.0.0.1:8761/
curl -fsS -o /dev/null -w '%{http_code}' http://127.0.0.1/
~~~

Human checks: the config health body contains UP, the Eureka page contains the word Eureka, and the gateway status code is 200.

## Out of scope

No new repository. No Spring Cloud Gateway replacement for Zuul. No Resilience4j replacement for Hystrix. No Spring Authorization Server replacement for spring-cloud-starter-oauth2. No deletion of Turbine. No Java 17 move. No edit to the MUST-reuse controllers, Feign clients, bootstrap.yml files, or shared yml files. Commit messages on the implementation branch name the todo ids they complete: P1.T1, P2.T1, P3.T1.
