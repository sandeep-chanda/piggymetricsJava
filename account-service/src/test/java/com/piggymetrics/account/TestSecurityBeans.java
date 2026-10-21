package com.piggymetrics.account;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

@AutoConfiguration
public class TestSecurityBeans {

	@Bean
	@Primary
	JwtDecoder jwtDecoder() {
		return token -> Jwt.withTokenValue(token)
				.headers(h -> h.put("alg", "none"))
				.claims(c -> {
					c.put("sub", "test");
					c.putAll(Map.of("scope", "server"));
				})
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();
	}
}
