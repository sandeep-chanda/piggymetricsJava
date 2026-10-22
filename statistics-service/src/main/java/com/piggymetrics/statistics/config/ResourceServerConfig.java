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
