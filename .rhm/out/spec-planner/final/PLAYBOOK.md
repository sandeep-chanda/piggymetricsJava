---
name: Walk 22 Oct fresh — Boot 3 platform line
overview: Upgrade piggymetricsJava to Spring Boot 3.5.16, Spring Cloud 2025.0.3, and
  Java 21; retire Zuul/Hystrix/Ribbon; migrate OAuth and Jakarta APIs; delete turbine-stream-service;
  gate on mvn test.
todos:
- id: p1-s001
  content: STEP-001 · P1.T1 Set parent Spring Boot 3.5.16 Spring Cloud 2025.0.3 Java
    21
  status: pending
- id: p1-s002
  content: STEP-002 · P1.T2 Re-resolve config starters on Boot 3 line (3 coordinates)
  status: pending
- id: p1-s003
  content: STEP-003 · P1.T3 Re-resolve monitoring starters on Boot 3 line (4 coordinates)
  status: pending
- id: p1-s004
  content: STEP-004 · P1.T4 Re-resolve registry starters on Boot 3 line (4 coordinates)
  status: pending
- id: p1-s005
  content: STEP-005 · P1.T5 Re-resolve gateway starters on Boot 3 line (7 coordinates)
  status: pending
- id: p1-s006
  content: STEP-006 · P1.T6 Re-resolve auth-service starters on Boot 3 line (12 coordinates)
  status: pending
- id: p1-s007
  content: STEP-007 · P1.T7 Re-resolve account-service starters on Boot 3 line (17
    coordinates)
  status: pending
- id: p1-s008
  content: STEP-008 · P1.T8 Re-resolve statistics-service starters on Boot 3 line
    (17 coordinates)
  status: pending
- id: p1-s009
  content: STEP-009 · P1.T9 Re-resolve notification-service starters on Boot 3 line
    (17 coordinates)
  status: pending
- id: p1-s010
  content: STEP-010 · P1.T10 Re-resolve turbine-stream-service starters on Boot 3
    line (6 coordinates)
  status: pending
- id: p1-s011
  content: STEP-011 · Raise every runtime Dockerfile FROM to eclipse-temurin Java
    21 JRE
  status: pending
- id: p1-s012
  content: STEP-012 · P2.T1 Remove EnableZuulProxy from GatewayApplication
  status: pending
- id: p1-s013
  content: STEP-013 · P2.T4-T18 Write gateway.yml Spring Cloud Gateway routes and
    timeouts
  status: pending
- id: p1-s014
  content: STEP-014 · P3.T3 Write shared application.yml Resilience4j timeout keys
  status: pending
- id: p1-s015
  content: STEP-015 · P3.T2 P3.T11 Write account-service.yml Feign circuitbreaker
    enabled
  status: pending
- id: p1-s016
  content: STEP-016 · P3.T1 Write AccountApplication without EnableCircuitBreaker
  status: pending
- id: p1-s017
  content: STEP-017 · P3.T5 P5.T1 Retire EnableHystrixDashboard on monitoring
  status: pending
- id: p1-s018
  content: STEP-018 · P5.T2 Named deletion of turbine-stream-service module
  status: pending
- id: p1-s019
  content: STEP-019 · Write config SecurityConfig as SecurityFilterChain requestMatchers
  status: pending
- id: p1-s020
  content: STEP-020 · Write auth WebSecurityConfig as SecurityFilterChain
  status: pending
- id: p1-s021
  content: STEP-021 · Write auth OAuth2AuthorizationConfig on Spring Authorization
    Server
  status: pending
- id: p1-s022
  content: STEP-022 · Write account ResourceServerConfig oauth2ResourceServer and
    Feign interceptor
  status: pending
- id: p1-s023
  content: STEP-023 · Write statistics ResourceServerConfig and StatisticsApplication
    on Boot 3
  status: pending
- id: p1-s024
  content: STEP-024 · Write notification ResourceServerConfig and application without
    legacy OAuth
  status: pending
- id: p1-s025
  content: STEP-025 · Move account-service javax.validation imports to jakarta.validation
  status: pending
- id: p1-s026
  content: STEP-026 · Move auth-service UserController to jakarta.validation
  status: pending
- id: p1-s027
  content: STEP-027 · Move statistics-service javax.validation imports to jakarta.validation
  status: pending
- id: p1-s028
  content: STEP-028 · Move notification-service javax.validation and javax.mail to
    jakarta
  status: pending
- id: p1-s029
  content: STEP-029 · P3.T12 P2.T19 Keep Feign fallbacks and green StatisticsServiceClientFallbackTest
  status: pending
- id: p1-s030
  content: STEP-030 · Run full reactor mvn test acceptance gate
  status: pending
isProject: false
---

# Walk 22 Oct fresh — Spring Boot 3 upgrade playbook

Plan Mode only: this playbook does not modify the target repository by itself. Implementers apply STEPs literally.

## Decisions locked

- **D-001** Parent line: Spring Boot `3.5.16` · Spring Cloud `2025.0.3` · Java `21`.
- **D-002** Every Java service Dockerfile `FROM eclipse-temurin:21-jre`.
- **D-004** Zuul successor: `spring-cloud-starter-gateway-server-webflux` with `spring.cloud.gateway.server.webflux.*`.
- **D-006** Hystrix successor: Spring Cloud CircuitBreaker Resilience4j + `spring.cloud.openfeign.circuitbreaker.enabled=true`.
- **D-007** Ribbon timeouts → Gateway `httpclient` connect/response timeouts; add `spring-cloud-starter-loadbalancer`.
- **D-008** OAuth → Spring Authorization Server + `oauth2ResourceServer` + OAuth2 client Feign interceptor.
- **D-009** monitoring stops reading Hystrix streams (Actuator host).
- **D-010** Named deletion of `turbine-stream-service`.
- **D-013** `javax.validation` / `javax.mail` → `jakarta.*`.

## Constraint

Gateway is WebFlux-only. Do not add `spring-boot-starter-web` to `gateway`. Final Done-when gate is `mvn test` (not `test-compile`, not `-DskipTests`). Snippets already declare every Maven coordinate the gate needs.

## Concrete changes by STEP

### STEP-001 — P1.T1 Set parent Spring Boot 3.5.16 Spring Cloud 2025.0.3 Java 21

Write the root reactor POM so spring-boot-starter-parent is 3.5.16, spring-cloud.version is 2025.0.3, and java.version is 21, keeping all nine modules listed for the P1 platform line including turbine-stream-service.

Files: `pom.xml`

exact_snippet (`pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
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
                <version>3.5.16</version>
                <relativePath/>
        </parent>

        <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <spring-cloud.version>2025.0.3</spring-cloud.version>
                <java.version>21</java.version>
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
mvn -q -N help:evaluate -Dexpression=project.parent.version -DforceStdout
~~~

Do not:
- Change module business Java sources in this step
- Lower java.version below 21
- Keep spring-boot-starter-parent at 2.0.3.RELEASE
- Ban edits to module Dockerfiles in later steps that raise the JVM image

Acceptance:
- pom.xml parent version equals 3.5.16
- pom.xml properties java.version equals 21
- pom.xml properties spring-cloud.version equals 2025.0.3

### STEP-002 — P1.T2 Re-resolve config starters on Boot 3 line (3 coordinates)

Write config/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `config/pom.xml`

exact_snippet (`config/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <artifactId>config</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>config</name>

        <parent>
                <groupId>com.piggymetrics</groupId>
                <artifactId>piggymetrics</artifactId>
                <version>1.0-SNAPSHOT</version>
        </parent>

        <dependencies>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-config-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-security</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation</artifactId>
                </dependency>
        </dependencies>

        <build>
                <plugins>
                        <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                                <configuration>
                                        <finalName>config</finalName>
                                </configuration>
                        </plugin>
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl config -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in config/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- config/pom.xml dependencies match the pinned exact_snippet
- mvn -pl config -am -DskipTests dependency:resolve exits 0

### STEP-003 — P1.T3 Re-resolve monitoring starters on Boot 3 line (4 coordinates)

Write monitoring/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `monitoring/pom.xml`

exact_snippet (`monitoring/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <artifactId>monitoring</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>monitoring</name>

        <parent>
                <groupId>com.piggymetrics</groupId>
                <artifactId>piggymetrics</artifactId>
                <version>1.0-SNAPSHOT</version>
        </parent>

        <dependencies>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-actuator</artifactId>
                </dependency>
                <dependency>
                        <groupId>io.github.resilience4j</groupId>
                        <artifactId>resilience4j-spring-boot3</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
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
                                        <finalName>monitoring</finalName>
                                </configuration>
                        </plugin>
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl monitoring -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in monitoring/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- monitoring/pom.xml dependencies match the pinned exact_snippet
- mvn -pl monitoring -am -DskipTests dependency:resolve exits 0

### STEP-004 — P1.T4 Re-resolve registry starters on Boot 3 line (4 coordinates)

Write registry/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `registry/pom.xml`

exact_snippet (`registry/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <artifactId>registry</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>registry</name>

        <parent>
                <groupId>com.piggymetrics</groupId>
                <artifactId>piggymetrics</artifactId>
                <version>1.0-SNAPSHOT</version>
        </parent>

        <dependencies>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-actuator</artifactId>
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
                                        <finalName>registry</finalName>
                                </configuration>
                        </plugin>
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl registry -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in registry/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- registry/pom.xml dependencies match the pinned exact_snippet
- mvn -pl registry -am -DskipTests dependency:resolve exits 0

### STEP-005 — P1.T5 Re-resolve gateway starters on Boot 3 line (7 coordinates)

Write gateway/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `gateway/pom.xml`

exact_snippet (`gateway/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <artifactId>gateway</artifactId>
        <version>1.0-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>gateway</name>

        <parent>
                <groupId>com.piggymetrics</groupId>
                <artifactId>piggymetrics</artifactId>
                <version>1.0-SNAPSHOT</version>
        </parent>

        <dependencies>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>io.micrometer</groupId>
                        <artifactId>micrometer-tracing-bridge-brave</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
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
                                        <finalName>gateway</finalName>
                                </configuration>
                        </plugin>
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl gateway -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in gateway/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- gateway/pom.xml dependencies match the pinned exact_snippet
- mvn -pl gateway -am -DskipTests dependency:resolve exits 0

### STEP-006 — P1.T6 Re-resolve auth-service starters on Boot 3 line (12 coordinates)

Write auth-service/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `auth-service/pom.xml`

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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
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
                        <groupId>io.micrometer</groupId>
                        <artifactId>micrometer-tracing-bridge-brave</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>de.flapdoodle.embed</groupId>
                        <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
                        <version>4.11.0</version>
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
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl auth-service -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in auth-service/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- auth-service/pom.xml dependencies match the pinned exact_snippet
- mvn -pl auth-service -am -DskipTests dependency:resolve exits 0

### STEP-007 — P1.T7 Re-resolve account-service starters on Boot 3 line (17 coordinates)

Write account-service/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `account-service/pom.xml`

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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-client</artifactId>
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
                        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
                </dependency>
                <dependency>
                        <groupId>io.micrometer</groupId>
                        <artifactId>micrometer-tracing-bridge-brave</artifactId>
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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>de.flapdoodle.embed</groupId>
                        <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
                        <version>4.11.0</version>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>com.jayway.jsonpath</groupId>
                        <artifactId>json-path</artifactId>
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
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl account-service -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in account-service/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- account-service/pom.xml dependencies match the pinned exact_snippet
- mvn -pl account-service -am -DskipTests dependency:resolve exits 0

### STEP-008 — P1.T8 Re-resolve statistics-service starters on Boot 3 line (17 coordinates)

Write statistics-service/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `statistics-service/pom.xml`

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
                        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-client</artifactId>
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
                        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
                </dependency>
                <dependency>
                        <groupId>com.google.guava</groupId>
                        <artifactId>guava</artifactId>
                        <version>33.4.0-jre</version>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
                </dependency>
                <dependency>
                        <groupId>io.micrometer</groupId>
                        <artifactId>micrometer-tracing-bridge-brave</artifactId>
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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>de.flapdoodle.embed</groupId>
                        <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
                        <version>4.11.0</version>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>com.jayway.jsonpath</groupId>
                        <artifactId>json-path</artifactId>
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
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl statistics-service -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in statistics-service/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- statistics-service/pom.xml dependencies match the pinned exact_snippet
- mvn -pl statistics-service -am -DskipTests dependency:resolve exits 0

### STEP-009 — P1.T9 Re-resolve notification-service starters on Boot 3 line (17 coordinates)

Write notification-service/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `notification-service/pom.xml`

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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-oauth2-client</artifactId>
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
                        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
                </dependency>
                <dependency>
                        <groupId>io.micrometer</groupId>
                        <artifactId>micrometer-tracing-bridge-brave</artifactId>
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
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>de.flapdoodle.embed</groupId>
                        <artifactId>de.flapdoodle.embed.mongo.spring30x</artifactId>
                        <version>4.11.0</version>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>com.jayway.jsonpath</groupId>
                        <artifactId>json-path</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-mail</artifactId>
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
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl notification-service -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in notification-service/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- notification-service/pom.xml dependencies match the pinned exact_snippet
- mvn -pl notification-service -am -DskipTests dependency:resolve exits 0

### STEP-010 — P1.T10 Re-resolve turbine-stream-service starters on Boot 3 line (6 coordinates)

Write turbine-stream-service/pom.xml dependency list so every starter resolves on Spring Boot 3.5.16 and Spring Cloud 2025.0.3 without Netflix Zuul Hystrix Ribbon oauth legacy artifacts retained beyond what later P2-P5 STEPs remove.

Files: `turbine-stream-service/pom.xml`

exact_snippet (`turbine-stream-service/pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>

        <artifactId>turbine-stream-service</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <packaging>jar</packaging>

        <name>turbine-stream-service</name>

        <parent>
                <groupId>com.piggymetrics</groupId>
                <artifactId>piggymetrics</artifactId>
                <version>1.0-SNAPSHOT</version>
        </parent>

        <dependencies>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-config</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.cloud</groupId>
                        <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-actuator</artifactId>
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
                                        <finalName>turbine-stream-service</finalName>
                                </configuration>
                        </plugin>
                </plugins>
        </build>

</project>
~~~

exact_commands:

~~~bash
mvn -pl turbine-stream-service -am -DskipTests dependency:resolve
~~~

Do not:
- Leave spring-cloud-starter-netflix-zuul in turbine-stream-service/pom.xml after P2 STEPs complete
- Pin Spring Boot or Spring Cloud versions inside the module POM
- Skip declaring coordinates that Java sources on the target line import

Acceptance:
- turbine-stream-service/pom.xml dependencies match the pinned exact_snippet
- mvn -pl turbine-stream-service -am -DskipTests dependency:resolve exits 0

### STEP-011 — Raise every runtime Dockerfile FROM to eclipse-temurin Java 21 JRE

Set every Java service runtime image FROM line to eclipse-temurin:21-jre so the container JVM major matches java.version 21 from the parent POM. Keep ADD jar path, CMD, EXPOSE, and MAINTAINER lines.

Files: `config/Dockerfile`, `monitoring/Dockerfile`, `registry/Dockerfile`, `gateway/Dockerfile`, `auth-service/Dockerfile`, `account-service/Dockerfile`, `statistics-service/Dockerfile`, `notification-service/Dockerfile`, `turbine-stream-service/Dockerfile`

exact_snippet (`config/Dockerfile`):

~~~dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/gateway.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/gateway.jar"]

EXPOSE 4000
~~~

exact_companion:

~~~text
# config/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/config.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/config.jar"]

EXPOSE 8888

---
# monitoring/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/monitoring.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/monitoring.jar"]

EXPOSE 8080

---
# registry/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/registry.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/registry.jar"]

EXPOSE 8761

---
# gateway/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/gateway.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/gateway.jar"]

EXPOSE 4000

---
# auth-service/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/auth-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/auth-service.jar"]

EXPOSE 5000

---
# account-service/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/account-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/account-service.jar"]

EXPOSE 6000

---
# statistics-service/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/statistics-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/statistics-service.jar"]

EXPOSE 7000

---
# notification-service/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/notification-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/notification-service.jar"]

EXPOSE 8000

---
# turbine-stream-service/Dockerfile
FROM eclipse-temurin:21-jre
MAINTAINER Alexander Lukyanchikov <sqshq@sqshq.com>

ADD ./target/turbine-stream-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/turbine-stream-service.jar"]

EXPOSE 8989
~~~

exact_commands:

~~~bash
rg -n '^FROM ' config/Dockerfile monitoring/Dockerfile registry/Dockerfile gateway/Dockerfile auth-service/Dockerfile account-service/Dockerfile statistics-service/Dockerfile notification-service/Dockerfile turbine-stream-service/Dockerfile
~~~

Do not:
- Leave any service Dockerfile on java:8-jre
- Change mongodb/Dockerfile in this step
- Forbid Dockerfile edits while raising java.version

Acceptance:
- Every listed Dockerfile FROM line equals eclipse-temurin:21-jre

### STEP-012 — P2.T1 Remove EnableZuulProxy from GatewayApplication

Write GatewayApplication without @EnableZuulProxy and without the netflix.zuul import. Spring Cloud Gateway server webflux auto-configuration activates from the starter on the classpath; discovery stays enabled.

Files: `gateway/src/main/java/com/piggymetrics/gateway/GatewayApplication.java`

exact_snippet (`gateway/src/main/java/com/piggymetrics/gateway/GatewayApplication.java`):

~~~java
package com.piggymetrics.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

        public static void main(String[] args) {
                SpringApplication.run(GatewayApplication.class, args);
        }
}
~~~

exact_commands:

~~~bash
mvn -pl gateway -am test
~~~

Do not:
- Retain @EnableZuulProxy
- Add servlet spring-boot-starter-web to gateway
- Invent a custom RouteLocator Java bean when YAML routes cover the four services

Acceptance:
- GatewayApplication.java contains no EnableZuulProxy symbol
- mvn -pl gateway -am test exits 0

### STEP-013 — P2.T4-T18 Write gateway.yml Spring Cloud Gateway routes and timeouts

Write config shared gateway.yml using spring.cloud.gateway.server.webflux routes for auth-service, account-service, statistics-service, and notification-service. Map former zuul.ignoredServices to discovery.locator.enabled false. Map zuul.host connect and socket timeouts to httpclient connect-timeout 20000 and response-timeout 20s. Use http://auth-service:5000 for auth and lb:// serviceIds for the three Eureka-backed services. Omit StripPrefix filters because former stripPrefix was false. Remove every zuul., ribbon., and hystrix. key from this file.

Files: `config/src/main/resources/shared/gateway.yml`

exact_snippet (`config/src/main/resources/shared/gateway.yml`):

~~~yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          discovery:
            locator:
              enabled: false
          httpclient:
            connect-timeout: 20000
            response-timeout: 20s
          routes:
            - id: auth-service
              uri: http://auth-service:5000
              predicates:
                - Path=/uaa/**
            - id: account-service
              uri: lb://account-service
              predicates:
                - Path=/accounts/**
            - id: statistics-service
              uri: lb://statistics-service
              predicates:
                - Path=/statistics/**
            - id: notification-service
              uri: lb://notification-service
              predicates:
                - Path=/notifications/**
    loadbalancer:
      enabled: true

resilience4j:
  timelimiter:
    configs:
      default:
        timeoutDuration: 20s

server:
  port: 4000
~~~

exact_commands:

~~~bash
rg -n 'zuul\.|ribbon\.|hystrix\.' config/src/main/resources/shared/gateway.yml; test $? -eq 1
~~~

Do not:
- Leave any zuul. key
- Leave any ribbon. key
- Leave any hystrix. key
- Use StripPrefix filters that would drop /accounts /statistics /notifications /uaa prefixes

Acceptance:
- gateway.yml has zero matches for zuul. ribbon. or hystrix. keys
- gateway.yml declares spring.cloud.gateway.server.webflux.routes for the four services
- Path predicates remain /uaa/** /accounts/** /statistics/** /notifications/**

### STEP-014 — P3.T3 Write shared application.yml Resilience4j timeout keys

Write shared application.yml so the former Hystrix 10000ms timeout is resilience4j.timelimiter.configs.default.timeoutDuration 10s, and migrate security.oauth2.resource.user-info-uri into spring.security.oauth2.resourceserver.opaque-token introspection settings used by resource servers on Boot 3.

Files: `config/src/main/resources/shared/application.yml`

exact_snippet (`config/src/main/resources/shared/application.yml`):

~~~yaml
logging:
  level:
    org.springframework.security: INFO

resilience4j:
  timelimiter:
    configs:
      default:
        timeoutDuration: 10s

eureka:
  instance:
    prefer-ip-address: true
  client:
    serviceUrl:
      defaultZone: http://registry:8761/eureka/

spring:
  security:
    oauth2:
      resourceserver:
        opaque-token:
          introspection-uri: http://auth-service:5000/uaa/oauth2/introspect
          client-id: account-service
          client-secret: ${ACCOUNT_SERVICE_PASSWORD}
  rabbitmq:
    host: rabbitmq
~~~

exact_commands:

~~~bash
rg -n '^hystrix:' config/src/main/resources/shared/application.yml; test $? -eq 1
~~~

Do not:
- Leave hystrix. keys in application.yml
- Keep security.oauth2.resource.user-info-uri as the sole resource-server property

Acceptance:
- application.yml has no hystrix keys
- resilience4j.timelimiter.configs.default.timeoutDuration equals 10s

### STEP-015 — P3.T2 P3.T11 Write account-service.yml Feign circuitbreaker enabled

Write account-service.yml with spring.cloud.openfeign.circuitbreaker.enabled true, Boot 3 spring.security.oauth2.client registration for account-service client_credentials, and the existing MongoDB and server.servlet.context-path settings. Remove feign.hystrix.enabled and legacy security.oauth2.client flat keys.

Files: `config/src/main/resources/shared/account-service.yml`

exact_snippet (`config/src/main/resources/shared/account-service.yml`):

~~~yaml
spring:
  security:
    oauth2:
      client:
        registration:
          account-service:
            client-id: account-service
            client-secret: ${ACCOUNT_SERVICE_PASSWORD}
            authorization-grant-type: client_credentials
            scope: server
        provider:
          auth-service:
            token-uri: http://auth-service:5000/uaa/oauth2/token
  data:
    mongodb:
      host: account-mongodb
      username: user
      password: ${MONGODB_PASSWORD}
      database: piggymetrics
      port: 27017
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

server:
  servlet:
    context-path: /accounts
  port: 6000
~~~

exact_commands:

~~~bash
rg -n 'feign\.hystrix|hystrix' config/src/main/resources/shared/account-service.yml; test $? -eq 1
~~~

Do not:
- Leave feign.hystrix.enabled
- Drop spring.cloud.openfeign.circuitbreaker.enabled

Acceptance:
- account-service.yml sets spring.cloud.openfeign.circuitbreaker.enabled to true
- No feign.hystrix key remains

### STEP-016 — P3.T1 Write AccountApplication without EnableCircuitBreaker

Write AccountApplication with @EnableFeignClients, @EnableDiscoveryClient, and @EnableMethodSecurity(prePostEnabled = true). Omit @EnableCircuitBreaker and @EnableOAuth2Client because Resilience4j Feign circuit breaking and oauth2 client auto-configuration activate from the starters.

Files: `account-service/src/main/java/com/piggymetrics/account/AccountApplication.java`

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/AccountApplication.java`):

~~~java
package com.piggymetrics.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableMethodSecurity(prePostEnabled = true)
public class AccountApplication {

        public static void main(String[] args) {
                SpringApplication.run(AccountApplication.class, args);
        }

}
~~~

exact_commands:

~~~bash
mvn -pl account-service -am test
~~~

Do not:
- Retain @EnableCircuitBreaker
- Retain @EnableOAuth2Client
- Retain @EnableGlobalMethodSecurity

Acceptance:
- AccountApplication has no EnableCircuitBreaker symbol
- AccountApplication has no EnableOAuth2Client symbol

### STEP-017 — P3.T5 P5.T1 Retire EnableHystrixDashboard on monitoring

Write MonitoringApplication as a plain @SpringBootApplication. Decision D-009 records that monitoring no longer consumes Hystrix streams; actuator and resilience4j-spring-boot3 from STEP-003 expose health only.

Files: `monitoring/src/main/java/com/piggymetrics/monitoring/MonitoringApplication.java`

exact_snippet (`monitoring/src/main/java/com/piggymetrics/monitoring/MonitoringApplication.java`):

~~~java
package com.piggymetrics.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonitoringApplication {

        public static void main(String[] args) {
                SpringApplication.run(MonitoringApplication.class, args);
        }
}
~~~

exact_commands:

~~~bash
mvn -pl monitoring -am test
~~~

Do not:
- Retain @EnableHystrixDashboard
- Reintroduce spring-cloud-starter-netflix-hystrix-dashboard

Acceptance:
- MonitoringApplication contains no EnableHystrixDashboard
- monitoring/pom.xml contains no hystrix-dashboard artifact

### STEP-018 — P5.T2 Named deletion of turbine-stream-service module

Write the root pom.xml modules list without turbine-stream-service (pinned final POM). Delete the turbine-stream-service module directory and remove turbine-stream-service service blocks from docker-compose.yml and docker-compose.dev.yml. Decision D-010 records named deletion because Turbine Stream has no Spring Cloud 2025 successor consuming Hystrix streams.

Files: `pom.xml`, `docker-compose.yml`, `docker-compose.dev.yml`

exact_snippet (`pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
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
                <version>3.5.16</version>
                <relativePath/>
        </parent>

        <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <spring-cloud.version>2025.0.3</spring-cloud.version>
                <java.version>21</java.version>
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
        </modules>

</project>
~~~

exact_companion:

~~~text
docker-compose.yml and docker-compose.dev.yml contain no turbine-stream-service service key; the turbine-stream-service/ directory is absent from the worktree.
~~~

exact_commands:

~~~bash
test ! -d turbine-stream-service
rg -n 'turbine-stream-service' pom.xml docker-compose.yml docker-compose.dev.yml; test $? -eq 1
~~~

Do not:
- Keep @EnableTurbineStream sources in the reactor
- Leave turbine-stream-service in parent modules
- Ban deletion of turbine-stream-service/pom.xml

Acceptance:
- pom.xml modules list omits turbine-stream-service
- turbine-stream-service directory is deleted
- compose files have no turbine-stream-service service

### STEP-019 — Write config SecurityConfig as SecurityFilterChain requestMatchers

Write SecurityConfig as a @Bean SecurityFilterChain using authorizeHttpRequests and requestMatchers("/actuator/**") plus httpBasic. Omit WebSecurityConfigurerAdapter and antMatchers.

Files: `config/src/main/java/com/piggymetrics/config/SecurityConfig.java`

exact_snippet (`config/src/main/java/com/piggymetrics/config/SecurityConfig.java`):

~~~java
package com.piggymetrics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable());
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated())
                                .httpBasic(Customizer.withDefaults());
                return http.build();
        }
}
~~~

exact_commands:

~~~bash
mvn -pl config -am -DskipTests compile
~~~

Do not:
- Extend WebSecurityConfigurerAdapter
- Call antMatchers
- Leave csrf enabled for config server

Acceptance:
- SecurityConfig.java contains SecurityFilterChain
- SecurityConfig.java contains no WebSecurityConfigurerAdapter
- SecurityConfig.java contains no antMatchers

### STEP-020 — Write auth WebSecurityConfig as SecurityFilterChain

Write WebSecurityConfig with SecurityFilterChain, PasswordEncoder BCryptPasswordEncoder bean, and AuthenticationManager from AuthenticationConfiguration. Wire MongoUserDetailsService via constructor. Omit WebSecurityConfigurerAdapter.

Files: `auth-service/src/main/java/com/piggymetrics/auth/config/WebSecurityConfig.java`

exact_snippet (`auth-service/src/main/java/com/piggymetrics/auth/config/WebSecurityConfig.java`):

~~~java
package com.piggymetrics.auth.config;

import com.piggymetrics.auth.service.security.MongoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

        private final MongoUserDetailsService userDetailsService;

        public WebSecurityConfig(MongoUserDetailsService userDetailsService) {
                this.userDetailsService = userDetailsService;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                                .csrf(csrf -> csrf.disable())
                                .userDetailsService(userDetailsService);
                return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}
~~~

exact_commands:

~~~bash
mvn -pl auth-service -am -DskipTests compile
~~~

Do not:
- Extend WebSecurityConfigurerAdapter
- Use NoOpPasswordEncoder for user passwords

Acceptance:
- WebSecurityConfig.java contains SecurityFilterChain
- WebSecurityConfig.java contains no WebSecurityConfigurerAdapter

### STEP-021 — Write auth OAuth2AuthorizationConfig on Spring Authorization Server

Write OAuth2AuthorizationConfig using RegisteredClientRepository, JWKSource, and AuthorizationServerSettings beans from spring-boot-starter-oauth2-authorization-server. Register browser, account-service, statistics-service, and notification-service clients with the same client IDs and server scope. Write AuthApplication without @EnableResourceServer using @EnableMethodSecurity.

Files: `auth-service/src/main/java/com/piggymetrics/auth/config/OAuth2AuthorizationConfig.java`, `auth-service/src/main/java/com/piggymetrics/auth/AuthApplication.java`

exact_snippet (`auth-service/src/main/java/com/piggymetrics/auth/config/OAuth2AuthorizationConfig.java`):

~~~java
package com.piggymetrics.auth.config;

import java.util.UUID;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

@Configuration
public class OAuth2AuthorizationConfig {

        private final Environment env;

        public OAuth2AuthorizationConfig(Environment env) {
                this.env = env;
        }

        @Bean
        RegisteredClientRepository registeredClientRepository() {
                RegisteredClient browser = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("browser")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .redirectUri("http://localhost:4000/login/oauth2/code/browser")
                                .scope("ui")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .build();
                RegisteredClient account = serviceClient("account-service", env.getProperty("ACCOUNT_SERVICE_PASSWORD"));
                RegisteredClient statistics = serviceClient("statistics-service", env.getProperty("STATISTICS_SERVICE_PASSWORD"));
                RegisteredClient notification = serviceClient("notification-service", env.getProperty("NOTIFICATION_SERVICE_PASSWORD"));
                return new InMemoryRegisteredClientRepository(browser, account, statistics, notification);
        }

        private static RegisteredClient serviceClient(String clientId, String secret) {
                return RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId(clientId)
                                .clientSecret("{noop}" + secret)
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                .scope("server")
                                .build();
        }

        @Bean
        JWKSource<SecurityContext> jwkSource() throws Exception {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                                .keyID(UUID.randomUUID().toString())
                                .build();
                return new ImmutableJWKSet<>(new JWKSet(rsaKey));
        }

        @Bean
        AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder().issuer("http://auth-service:5000/uaa").build();
        }
}
~~~

exact_companion:

~~~text
package com.piggymetrics.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMethodSecurity(prePostEnabled = true)
public class AuthApplication {

        public static void main(String[] args) {
                SpringApplication.run(AuthApplication.class, args);
        }

}
~~~

exact_commands:

~~~bash
mvn -pl auth-service -am test
~~~

Do not:
- Retain @EnableAuthorizationServer
- Retain AuthorizationServerConfigurerAdapter
- Retain spring-cloud-starter-oauth2 on auth-service
- Omit spring-boot-starter-oauth2-authorization-server coordinate

Acceptance:
- OAuth2AuthorizationConfig contains RegisteredClientRepository bean
- AuthApplication contains no EnableResourceServer
- auth-service/pom.xml contains spring-boot-starter-oauth2-authorization-server

### STEP-022 — Write account ResourceServerConfig oauth2ResourceServer and Feign interceptor

Write ResourceServerConfig with SecurityFilterChain using requestMatchers("/", "/demo") permitAll and oauth2ResourceServer opaqueToken. Provide OAuth2AuthorizedClientManager and a Feign RequestInterceptor that authorizes the account-service client registration. Delete CustomUserInfoTokenServices.java because opaque token introspection replaces ResourceServerTokenServices.

Files: `account-service/src/main/java/com/piggymetrics/account/config/ResourceServerConfig.java`, `account-service/src/main/java/com/piggymetrics/account/service/security/CustomUserInfoTokenServices.java`

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/config/ResourceServerConfig.java`):

~~~java
package com.piggymetrics.account.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/", "/demo").permitAll()
                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()));
                return http.build();
        }

        @Bean
        OAuth2AuthorizedClientManager authorizedClientManager(
                        ClientRegistrationRepository clients,
                        OAuth2AuthorizedClientRepository authorizedClients) {
                return new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
        }

        @Bean
        RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager manager) {
                return new RequestInterceptor() {
                        @Override
                        public void apply(RequestTemplate template) {
                                OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                                                .withClientRegistrationId("account-service")
                                                .principal("account-service")
                                                .build();
                                var client = manager.authorize(request);
                                if (client != null && client.getAccessToken() != null) {
                                        template.header("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
                                }
                        }
                };
        }
}
~~~

exact_companion:

~~~text
DELETED_FILE: account-service/src/main/java/com/piggymetrics/account/service/security/CustomUserInfoTokenServices.java
~~~

exact_commands:

~~~bash
test ! -f account-service/src/main/java/com/piggymetrics/account/service/security/CustomUserInfoTokenServices.java
mvn -pl account-service -am test
~~~

Do not:
- Retain @EnableResourceServer
- Retain OAuth2FeignRequestInterceptor
- Retain antMatchers
- Keep CustomUserInfoTokenServices after this step

Acceptance:
- ResourceServerConfig uses oauth2ResourceServer
- CustomUserInfoTokenServices.java is deleted
- No OAuth2FeignRequestInterceptor import remains in account-service

### STEP-023 — Write statistics ResourceServerConfig and StatisticsApplication on Boot 3

Write statistics ResourceServerConfig with oauth2ResourceServer opaqueToken SecurityFilterChain. Write StatisticsApplication with @EnableMethodSecurity and MongoCustomConversions bean, omitting @EnableOAuth2Client and legacy ResourceServerTokenServices imports. Delete statistics CustomUserInfoTokenServices.java.

Files: `statistics-service/src/main/java/com/piggymetrics/statistics/config/ResourceServerConfig.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/StatisticsApplication.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/service/security/CustomUserInfoTokenServices.java`

exact_snippet (`statistics-service/src/main/java/com/piggymetrics/statistics/config/ResourceServerConfig.java`):

~~~java
package com.piggymetrics.statistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()));
                return http.build();
        }
}
~~~

exact_companion:

~~~text
package com.piggymetrics.statistics;

import com.piggymetrics.statistics.repository.converter.DataPointIdReaderConverter;
import com.piggymetrics.statistics.repository.converter.DataPointIdWriterConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableMethodSecurity(prePostEnabled = true)
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

---
DELETED_FILE: statistics-service/src/main/java/com/piggymetrics/statistics/service/security/CustomUserInfoTokenServices.java
~~~

exact_commands:

~~~bash
mvn -pl statistics-service -am test
~~~

Do not:
- Retain @EnableResourceServer in statistics-service
- Retain @EnableOAuth2Client
- Keep CustomConversions constructor API from Spring Data MongoDB 2.x

Acceptance:
- statistics ResourceServerConfig uses oauth2ResourceServer
- StatisticsApplication uses MongoCustomConversions
- statistics CustomUserInfoTokenServices.java is deleted

### STEP-024 — Write notification ResourceServerConfig and application without legacy OAuth

Write notification ResourceServerConfig with oauth2ResourceServer SecurityFilterChain and Feign RequestInterceptor using OAuth2AuthorizedClientManager for notification-service registration. Write NotificationServiceApplication with @EnableMethodSecurity and without @EnableOAuth2Client.

Files: `notification-service/src/main/java/com/piggymetrics/notification/config/ResourceServerConfig.java`, `notification-service/src/main/java/com/piggymetrics/notification/NotificationServiceApplication.java`

exact_snippet (`notification-service/src/main/java/com/piggymetrics/notification/config/ResourceServerConfig.java`):

~~~java
package com.piggymetrics.notification.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()));
                return http.build();
        }

        @Bean
        OAuth2AuthorizedClientManager authorizedClientManager(
                        ClientRegistrationRepository clients,
                        OAuth2AuthorizedClientRepository authorizedClients) {
                return new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
        }

        @Bean
        RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager manager) {
                return template -> {
                        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                                        .withClientRegistrationId("notification-service")
                                        .principal("notification-service")
                                        .build();
                        var client = manager.authorize(request);
                        if (client != null && client.getAccessToken() != null) {
                                template.header("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
                        }
                };
        }
}
~~~

exact_companion:

~~~text
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Arrays;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableMethodSecurity(prePostEnabled = true)
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
mvn -pl notification-service -am test
~~~

Do not:
- Retain OAuth2FeignRequestInterceptor
- Retain @EnableResourceServer
- Omit spring-boot-starter-oauth2-client from notification-service/pom.xml

Acceptance:
- notification ResourceServerConfig contains oauth2ResourceServer
- NotificationServiceApplication contains no EnableOAuth2Client

### STEP-025 — Move account-service javax.validation imports to jakarta.validation

Write the five account-service Java sources so every javax.validation import becomes jakarta.validation. Keep Hibernate @Length imports unchanged. spring-boot-starter-validation is already declared in account-service/pom.xml from STEP-007.

Files: `account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java`, `account-service/src/main/java/com/piggymetrics/account/domain/Account.java`, `account-service/src/main/java/com/piggymetrics/account/domain/Item.java`, `account-service/src/main/java/com/piggymetrics/account/domain/Saving.java`, `account-service/src/main/java/com/piggymetrics/account/domain/User.java`

exact_snippet (`account-service/src/main/java/com/piggymetrics/account/controller/AccountController.java`):

~~~java
package com.piggymetrics.account.controller;

import com.piggymetrics.account.domain.Account;
import com.piggymetrics.account.domain.User;
import com.piggymetrics.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
}
~~~

exact_companion:

~~~text
# account-service/src/main/java/com/piggymetrics/account/domain/Account.java
package com.piggymetrics.account.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
}

---
# account-service/src/main/java/com/piggymetrics/account/domain/Item.java
package com.piggymetrics.account.domain;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Item {

  @NotNull
  @Length(min = 1, max = 20)
  private String title;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private Currency currency;

  @NotNull
  private TimePeriod period;

  @NotNull
  private String icon;

  public String getTitle() {
  	return title;
  }

  public void setTitle(String title) {
  	this.title = title;
  }

  public BigDecimal getAmount() {
  	return amount;
  }

  public void setAmount(BigDecimal amount) {
  	this.amount = amount;
  }

  public Currency getCurrency() {
  	return currency;
  }

  public void setCurrency(Currency currency) {
  	this.currency = currency;
  }

  public TimePeriod getPeriod() {
  	return period;
  }

  public void setPeriod(TimePeriod period) {
  	this.period = period;
  }

  public String getIcon() {
  	return icon;
  }

  public void setIcon(String icon) {
  	this.icon = icon;
  }
}

---
# account-service/src/main/java/com/piggymetrics/account/domain/Saving.java
package com.piggymetrics.account.domain;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Saving {

  @NotNull
  private BigDecimal amount;

  @NotNull
  private Currency currency;

  @NotNull
  private BigDecimal interest;

  @NotNull
  private Boolean deposit;

  @NotNull
  private Boolean capitalization;

  public BigDecimal getAmount() {
  	return amount;
  }

  public void setAmount(BigDecimal amount) {
  	this.amount = amount;
  }

  public Currency getCurrency() {
  	return currency;
  }

  public void setCurrency(Currency currency) {
  	this.currency = currency;
  }

  public BigDecimal getInterest() {
  	return interest;
  }

  public void setInterest(BigDecimal interest) {
  	this.interest = interest;
  }

  public Boolean getDeposit() {
  	return deposit;
  }

  public void setDeposit(Boolean deposit) {
  	this.deposit = deposit;
  }

  public Boolean getCapitalization() {
  	return capitalization;
  }

  public void setCapitalization(Boolean capitalization) {
  	this.capitalization = capitalization;
  }
}

---
# account-service/src/main/java/com/piggymetrics/account/domain/User.java
package com.piggymetrics.account.domain;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

public class User {

  @NotNull
  @Length(min = 3, max = 20)
  private String username;

  @NotNull
  @Length(min = 6, max = 40)
  private String password;

  public String getUsername() {
  	return username;
  }

  public void setUsername(String username) {
  	this.username = username;
  }

  public String getPassword() {
  	return password;
  }

  public void setPassword(String password) {
  	this.password = password;
  }
}
~~~

exact_commands:

~~~bash
rg -n 'javax\.validation' account-service/src/main; test $? -eq 1
~~~

Do not:
- Leave javax.validation imports in account-service main sources
- Change validation min/max attribute values

Acceptance:
- No javax.validation import remains under account-service/src/main
- Each listed file matches its pinned final text

### STEP-026 — Move auth-service UserController to jakarta.validation

Write UserController.java with jakarta.validation.Valid. Keep #oauth2 SpEL migration to hasAuthority('SCOPE_server') in the same final text when PreAuthorize is present.

Files: `auth-service/src/main/java/com/piggymetrics/auth/controller/UserController.java`

exact_snippet (`auth-service/src/main/java/com/piggymetrics/auth/controller/UserController.java`):

~~~java
package com.piggymetrics.auth.controller;

import com.piggymetrics.auth.domain.User;
import com.piggymetrics.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/current", method = RequestMethod.GET)
  public Principal getUser(Principal principal) {
  	return principal;
  }

  @PreAuthorize("hasAuthority('SCOPE_server')")
  @RequestMapping(method = RequestMethod.POST)
  public void createUser(@Valid @RequestBody User user) {
  	userService.create(user);
  }
}
~~~

exact_commands:

~~~bash
rg -n 'javax\.validation' auth-service/src/main; test $? -eq 1
~~~

Do not:
- Leave javax.validation in UserController

Acceptance:
- UserController imports jakarta.validation.Valid

### STEP-027 — Move statistics-service javax.validation imports to jakarta.validation

Write the four statistics-service Java sources with jakarta.validation imports and SCOPE_server authority checks where #oauth2.hasScope was used.

Files: `statistics-service/src/main/java/com/piggymetrics/statistics/controller/StatisticsController.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/domain/Account.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/domain/Item.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/domain/Saving.java`

exact_snippet (`statistics-service/src/main/java/com/piggymetrics/statistics/controller/StatisticsController.java`):

~~~java
package com.piggymetrics.statistics.controller;

import com.piggymetrics.statistics.domain.Account;
import com.piggymetrics.statistics.domain.timeseries.DataPoint;
import com.piggymetrics.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
public class StatisticsController {

  @Autowired
  private StatisticsService statisticsService;

  @RequestMapping(value = "/current", method = RequestMethod.GET)
  public List<DataPoint> getCurrentAccountStatistics(Principal principal) {
  	return statisticsService.findByAccountName(principal.getName());
  }

  @PreAuthorize("hasAuthority('SCOPE_server') or #accountName.equals('demo')")
  @RequestMapping(value = "/{accountName}", method = RequestMethod.GET)
  public List<DataPoint> getStatisticsByAccountName(@PathVariable String accountName) {
  	return statisticsService.findByAccountName(accountName);
  }

  @PreAuthorize("hasAuthority('SCOPE_server')")
  @RequestMapping(value = "/{accountName}", method = RequestMethod.PUT)
  public void saveAccountStatistics(@PathVariable String accountName, @Valid @RequestBody Account account) {
  	statisticsService.save(accountName, account);
  }
}
~~~

exact_companion:

~~~text
# statistics-service/src/main/java/com/piggymetrics/statistics/domain/Account.java
package com.piggymetrics.statistics.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

  @Valid
  @NotNull
  private List<Item> incomes;

  @Valid
  @NotNull
  private List<Item> expenses;

  @Valid
  @NotNull
  private Saving saving;

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
}

---
# statistics-service/src/main/java/com/piggymetrics/statistics/domain/Item.java
package com.piggymetrics.statistics.domain;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Item {

  @NotNull
  @Length(min = 1, max = 20)
  private String title;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private Currency currency;

  @NotNull
  private TimePeriod period;

  public String getTitle() {
  	return title;
  }

  public void setTitle(String title) {
  	this.title = title;
  }

  public BigDecimal getAmount() {
  	return amount;
  }

  public void setAmount(BigDecimal amount) {
  	this.amount = amount;
  }

  public Currency getCurrency() {
  	return currency;
  }

  public void setCurrency(Currency currency) {
  	this.currency = currency;
  }

  public TimePeriod getPeriod() {
  	return period;
  }

  public void setPeriod(TimePeriod period) {
  	this.period = period;
  }
}

---
# statistics-service/src/main/java/com/piggymetrics/statistics/domain/Saving.java
package com.piggymetrics.statistics.domain;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Saving {

  @NotNull
  private BigDecimal amount;

  @NotNull
  private Currency currency;

  @NotNull
  private BigDecimal interest;

  @NotNull
  private Boolean deposit;

  @NotNull
  private Boolean capitalization;

  public BigDecimal getAmount() {
  	return amount;
  }

  public void setAmount(BigDecimal amount) {
  	this.amount = amount;
  }

  public Currency getCurrency() {
  	return currency;
  }

  public void setCurrency(Currency currency) {
  	this.currency = currency;
  }

  public BigDecimal getInterest() {
  	return interest;
  }

  public void setInterest(BigDecimal interest) {
  	this.interest = interest;
  }

  public Boolean getDeposit() {
  	return deposit;
  }

  public void setDeposit(Boolean deposit) {
  	this.deposit = deposit;
  }

  public Boolean getCapitalization() {
  	return capitalization;
  }

  public void setCapitalization(Boolean capitalization) {
  	this.capitalization = capitalization;
  }
}
~~~

exact_commands:

~~~bash
rg -n 'javax\.validation' statistics-service/src/main; test $? -eq 1
~~~

Do not:
- Leave javax.validation under statistics-service/src/main

Acceptance:
- No javax.validation import remains under statistics-service/src/main

### STEP-028 — Move notification-service javax.validation and javax.mail to jakarta

Write notification controller, domain, EmailService, EmailServiceImpl, and the two mail-related tests so javax.validation and javax.mail become jakarta.validation and jakarta.mail. Map Hibernate Email constraint to jakarta.validation.constraints.Email on Recipient.

Files: `notification-service/src/main/java/com/piggymetrics/notification/controller/RecipientController.java`, `notification-service/src/main/java/com/piggymetrics/notification/domain/NotificationSettings.java`, `notification-service/src/main/java/com/piggymetrics/notification/domain/Recipient.java`, `notification-service/src/main/java/com/piggymetrics/notification/service/EmailService.java`, `notification-service/src/main/java/com/piggymetrics/notification/service/EmailServiceImpl.java`, `notification-service/src/test/java/com/piggymetrics/notification/service/EmailServiceImplTest.java`, `notification-service/src/test/java/com/piggymetrics/notification/service/NotificationServiceImplTest.java`

exact_snippet (`notification-service/src/main/java/com/piggymetrics/notification/controller/RecipientController.java`):

~~~java
package com.piggymetrics.notification.controller;

import com.piggymetrics.notification.domain.Recipient;
import com.piggymetrics.notification.service.RecipientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

  @Autowired
  private RecipientService recipientService;

  @RequestMapping(path = "/current", method = RequestMethod.GET)
  public Object getCurrentNotificationsSettings(Principal principal) {
  	return recipientService.findByAccountName(principal.getName());
  }

  @RequestMapping(path = "/current", method = RequestMethod.PUT)
  public Object saveCurrentNotificationsSettings(Principal principal, @Valid @RequestBody Recipient recipient) {
  	return recipientService.save(principal.getName(), recipient);
  }
}
~~~

exact_companion:

~~~text
# notification-service/src/main/java/com/piggymetrics/notification/domain/NotificationSettings.java
package com.piggymetrics.notification.domain;

import jakarta.validation.constraints.NotNull;
import java.util.Date;

public class NotificationSettings {

  @NotNull
  private Boolean active;

  @NotNull
  private Frequency frequency;

  private Date lastNotified;

  public Boolean getActive() {
  	return active;
  }

  public void setActive(Boolean active) {
  	this.active = active;
  }

  public Frequency getFrequency() {
  	return frequency;
  }

  public void setFrequency(Frequency frequency) {
  	this.frequency = frequency;
  }

  public Date getLastNotified() {
  	return lastNotified;
  }

  public void setLastNotified(Date lastNotified) {
  	this.lastNotified = lastNotified;
  }
}

---
# notification-service/src/main/java/com/piggymetrics/notification/domain/Recipient.java
package com.piggymetrics.notification.domain;

import jakarta.validation.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Document(collection = "recipients")
public class Recipient {

  @Id
  private String accountName;

  @NotNull
  @Email
  private String email;

  @Valid
  private Map<NotificationType, NotificationSettings> scheduledNotifications;

  public String getAccountName() {
  	return accountName;
  }

  public void setAccountName(String accountName) {
  	this.accountName = accountName;
  }

  public String getEmail() {
  	return email;
  }

  public void setEmail(String email) {
  	this.email = email;
  }

  public Map<NotificationType, NotificationSettings> getScheduledNotifications() {
  	return scheduledNotifications;
  }

  public void setScheduledNotifications(Map<NotificationType, NotificationSettings> scheduledNotifications) {
  	this.scheduledNotifications = scheduledNotifications;
  }

  @Override
  public String toString() {
  	return "Recipient{" +
  			"accountName='" + accountName + '\'' +
  			", email='" + email + '\'' +
  			'}';
  }
}

---
# notification-service/src/main/java/com/piggymetrics/notification/service/EmailService.java
package com.piggymetrics.notification.service;

import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;

import jakarta.mail.MessagingException;
import java.io.IOException;

public interface EmailService {

  void send(NotificationType type, Recipient recipient, String attachment) throws MessagingException, IOException;

}

---
# notification-service/src/main/java/com/piggymetrics/notification/service/EmailServiceImpl.java
package com.piggymetrics.notification.service;

import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.MessageFormat;

@Service
@RefreshScope
public class EmailServiceImpl implements EmailService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private Environment env;

  @Override
  public void send(NotificationType type, Recipient recipient, String attachment) throws MessagingException, IOException {

  	final String subject = env.getProperty(type.getSubject());
  	final String text = MessageFormat.format(env.getProperty(type.getText()), recipient.getAccountName());

  	MimeMessage message = mailSender.createMimeMessage();

  	MimeMessageHelper helper = new MimeMessageHelper(message, true);
  	helper.setTo(recipient.getEmail());
  	helper.setSubject(subject);
  	helper.setText(text);

  	if (StringUtils.hasLength(attachment)) {
  		helper.addAttachment(env.getProperty(type.getAttachment()), new ByteArrayResource(attachment.getBytes()));
  	}

  	mailSender.send(message);

  	log.info("{} email notification has been send to {}", type, recipient.getEmail());
  }
}

---
# notification-service/src/test/java/com/piggymetrics/notification/service/EmailServiceImplTest.java
package com.piggymetrics.notification.service;

import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmailServiceImplTest {

  @InjectMocks
  private EmailServiceImpl emailService;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private Environment env;

  @Captor
  private ArgumentCaptor<MimeMessage> captor;

  @Before
  public void setup() {
  	initMocks(this);
  	when(mailSender.createMimeMessage())
  			.thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
  }

  @Test
  public void shouldSendBackupEmail() throws MessagingException, IOException {

  	final String subject = "subject";
  	final String text = "text";
  	final String attachment = "attachment.json";

  	Recipient recipient = new Recipient();
  	recipient.setAccountName("test");
  	recipient.setEmail("test@test.com");

  	when(env.getProperty(NotificationType.BACKUP.getSubject())).thenReturn(subject);
  	when(env.getProperty(NotificationType.BACKUP.getText())).thenReturn(text);
  	when(env.getProperty(NotificationType.BACKUP.getAttachment())).thenReturn(attachment);

  	emailService.send(NotificationType.BACKUP, recipient, "{\"name\":\"test\"");

  	verify(mailSender).send(captor.capture());

  	MimeMessage message = captor.getValue();
  	assertEquals(subject, message.getSubject());
  	// TODO check other fields
  }

  @Test
  public void shouldSendRemindEmail() throws MessagingException, IOException {

  	final String subject = "subject";
  	final String text = "text";

  	Recipient recipient = new Recipient();
  	recipient.setAccountName("test");
  	recipient.setEmail("test@test.com");

  	when(env.getProperty(NotificationType.REMIND.getSubject())).thenReturn(subject);
  	when(env.getProperty(NotificationType.REMIND.getText())).thenReturn(text);

  	emailService.send(NotificationType.REMIND, recipient, null);

  	verify(mailSender).send(captor.capture());

  	MimeMessage message = captor.getValue();
  	assertEquals(subject, message.getSubject());
  	// TODO check other fields
  }
}
---
# notification-service/src/test/java/com/piggymetrics/notification/service/NotificationServiceImplTest.java
package com.piggymetrics.notification.service;

import com.google.common.collect.ImmutableList;
import com.piggymetrics.notification.client.AccountServiceClient;
import com.piggymetrics.notification.domain.NotificationType;
import com.piggymetrics.notification.domain.Recipient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.mail.MessagingException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class NotificationServiceImplTest {

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Mock
  private RecipientService recipientService;

  @Mock
  private AccountServiceClient client;

  @Mock
  private EmailService emailService;

  @Before
  public void setup() {
  	initMocks(this);
  }

  @Test
  public void shouldSendBackupNotificationsEvenWhenErrorsOccursForSomeRecipients() throws IOException, MessagingException, InterruptedException {

  	final String attachment = "json";

  	Recipient withError = new Recipient();
  	withError.setAccountName("with-error");

  	Recipient withNoError = new Recipient();
  	withNoError.setAccountName("with-no-error");

  	when(client.getAccount(withError.getAccountName())).thenThrow(new RuntimeException());
  	when(client.getAccount(withNoError.getAccountName())).thenReturn(attachment);

  	when(recipientService.findReadyToNotify(NotificationType.BACKUP)).thenReturn(ImmutableList.of(withNoError, withError));

  	notificationService.sendBackupNotifications();

  	// TODO test concurrent code in a right way

  	verify(emailService, timeout(100)).send(NotificationType.BACKUP, withNoError, attachment);
  	verify(recipientService, timeout(100)).markNotified(NotificationType.BACKUP, withNoError);

  	verify(recipientService, never()).markNotified(NotificationType.BACKUP, withError);
  }

  @Test
  public void shouldSendRemindNotificationsEvenWhenErrorsOccursForSomeRecipients() throws IOException, MessagingException, InterruptedException {

  	final String attachment = "json";

  	Recipient withError = new Recipient();
  	withError.setAccountName("with-error");

  	Recipient withNoError = new Recipient();
  	withNoError.setAccountName("with-no-error");

  	when(recipientService.findReadyToNotify(NotificationType.REMIND)).thenReturn(ImmutableList.of(withNoError, withError));
  	doThrow(new RuntimeException()).when(emailService).send(NotificationType.REMIND, withError, null);

  	notificationService.sendRemindNotifications();

  	// TODO test concurrent code in a right way

  	verify(emailService, timeout(100)).send(NotificationType.REMIND, withNoError, null);
  	verify(recipientService, timeout(100)).markNotified(NotificationType.REMIND, withNoError);

  	verify(recipientService, never()).markNotified(NotificationType.REMIND, withError);
  }
}
~~~

exact_commands:

~~~bash
rg -n 'javax\.(validation|mail)' notification-service/src; test $? -eq 1
~~~

Do not:
- Leave javax.validation or javax.mail imports in notification-service sources listed here

Acceptance:
- No javax.validation or javax.mail imports remain under notification-service/src

### STEP-029 — P3.T12 P2.T19 Keep Feign fallbacks and green StatisticsServiceClientFallbackTest

Keep StatisticsServiceClientFallback and ExchangeRatesClientFallback as the Feign fallback classes. Write StatisticsServiceClient and AuthServiceClient consuming APPLICATION_JSON_VALUE. Write StatisticsServiceClientFallbackTest on JUnit 5 with spring.cloud.openfeign.circuitbreaker.enabled=true and OutputCaptureExtension. Keep ExchangeRatesClient fallback attribute. Write AccountServiceClient on APPLICATION_JSON_VALUE when it referenced UTF8.

Files: `account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClient.java`, `account-service/src/main/java/com/piggymetrics/account/client/StatisticsServiceClientFallback.java`, `account-service/src/main/java/com/piggymetrics/account/client/AuthServiceClient.java`, `account-service/src/test/java/com/piggymetrics/account/client/StatisticsServiceClientFallbackTest.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/client/ExchangeRatesClient.java`, `statistics-service/src/main/java/com/piggymetrics/statistics/client/ExchangeRatesClientFallback.java`, `notification-service/src/main/java/com/piggymetrics/notification/client/AccountServiceClient.java`

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

  @RequestMapping(method = RequestMethod.PUT, value = "/statistics/{accountName}", consumes = MediaType.APPLICATION_JSON_VALUE)
  void updateStatistics(@PathVariable("accountName") String accountName, Account account);

}
~~~

exact_companion:

~~~text
# StatisticsServiceClientFallback.java unchanged component body retained
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author cdov
 */
@Component
public class StatisticsServiceClientFallback implements StatisticsServiceClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceClientFallback.class);
    @Override
    public void updateStatistics(String accountName, Account account) {
        LOGGER.error("Error during update statistics for account: {}", accountName);
    }
}

---
# StatisticsServiceClientFallbackTest.java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(properties = {
        "spring.cloud.openfeign.circuitbreaker.enabled=true"
})
public class StatisticsServiceClientFallbackTest {
    @Autowired
    private StatisticsServiceClient statisticsServiceClient;

    @Test
    public void testUpdateStatisticsWithFailFallback(CapturedOutput output){
        statisticsServiceClient.updateStatistics("test", new Account());
        assertThat(output).contains("Error during update statistics for account: test");
    }
}

---
# AuthServiceClient.java
package com.piggymetrics.account.client;

import com.piggymetrics.account.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

  @RequestMapping(method = RequestMethod.POST, value = "/uaa/users", consumes = MediaType.APPLICATION_JSON_VALUE)
  void createUser(User user);

}

---
# ExchangeRatesClient.java
package com.piggymetrics.statistics.client;

import com.piggymetrics.statistics.domain.Currency;
import com.piggymetrics.statistics.domain.ExchangeRatesContainer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${rates.url}", name = "rates-client", fallback = ExchangeRatesClientFallback.class)
public interface ExchangeRatesClient {

    @RequestMapping(method = RequestMethod.GET, value = "/latest")
    ExchangeRatesContainer getRates(@RequestParam("base") Currency base);

}

---
# ExchangeRatesClientFallback.java
package com.piggymetrics.statistics.client;

import com.piggymetrics.statistics.domain.Currency;
import com.piggymetrics.statistics.domain.ExchangeRatesContainer;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ExchangeRatesClientFallback implements ExchangeRatesClient {

    @Override
    public ExchangeRatesContainer getRates(Currency base) {
        ExchangeRatesContainer container = new ExchangeRatesContainer();
        container.setBase(Currency.getBase());
        container.setRates(Collections.emptyMap());
        return container;
    }
}

---
# AccountServiceClient.java
package com.piggymetrics.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

  @RequestMapping(method = RequestMethod.GET, value = "/accounts/{accountName}", consumes = MediaType.APPLICATION_JSON_VALUE)
  String getAccount(@PathVariable("accountName") String accountName);

}
~~~

exact_commands:

~~~bash
mvn -pl account-service -am -Dtest=StatisticsServiceClientFallbackTest test
~~~

Do not:
- Remove fallback= from StatisticsServiceClient or ExchangeRatesClient
- Keep feign.hystrix.enabled in the test properties
- Lower acceptance to test-compile

Acceptance:
- StatisticsServiceClientFallbackTest passes under mvn test
- StatisticsServiceClient still declares fallback = StatisticsServiceClientFallback.class
- ExchangeRatesClient still declares fallback = ExchangeRatesClientFallback.class
- Test properties use spring.cloud.openfeign.circuitbreaker.enabled=true

### STEP-030 — Run full reactor mvn test acceptance gate

After prior STEPs, run the reactor test gate. Acceptance is mvn test across the remaining modules (turbine-stream-service already deleted). Do not use -DskipTests or test-compile as the Done-when gate.

Files: `pom.xml`

exact_snippet (`pom.xml`):

~~~xml
<?xml version="1.0" encoding="UTF-8"?>
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
                <version>3.5.16</version>
                <relativePath/>
        </parent>

        <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <spring-cloud.version>2025.0.3</spring-cloud.version>
                <java.version>21</java.version>
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
        </modules>

</project>
~~~

exact_commands:

~~~bash
mvn test
~~~

Do not:
- Use mvn test-compile as the Done-when gate
- Use -DskipTests for the final acceptance
- Re-add Zuul Hystrix Ribbon markers

Acceptance:
- mvn test exits 0 for the reactor
- rg finds no zuul.|EnableZuulProxy|spring-cloud-starter-netflix-zuul
- rg finds no hystrix.|EnableHystrix|EnableCircuitBreaker|spring-cloud-starter-netflix-hystrix
- rg finds no ribbon.ReadTimeout|ribbon.ConnectTimeout

## Verify

exact_commands:

~~~bash
mvn test
~~~

Also confirm zero matches for `zuul.`, `EnableZuulProxy`, `hystrix.`, `EnableHystrix`, `EnableCircuitBreaker`, `ribbon.ReadTimeout`, `ribbon.ConnectTimeout`, and `javax.validation` under main sources.

## Out of scope

- Rewriting Mongo document schemas
- Changing public HTTP path prefixes (`/uaa`, `/accounts`, `/statistics`, `/notifications`)
- Adding new business endpoints

