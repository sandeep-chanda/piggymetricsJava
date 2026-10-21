package com.piggymetrics.statistics.config;

import com.piggymetrics.statistics.service.security.CustomUserInfoTokenServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author cdov
 */
@EnableWebSecurity
@Configuration
public class ResourceServerConfig {

	@Bean
	public SecurityFilterChain statisticsSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()))
				.csrf(csrf -> csrf.disable());
		return http.build();
	}

	@Bean
	public OpaqueTokenIntrospector opaqueTokenIntrospector(
			@Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://auth-service:5000/uaa/users/current}") String userInfoUri,
			@Value("${spring.security.oauth2.client.registration.statistics-service.client-id:statistics-service}") String clientId) {
		return new CustomUserInfoTokenServices(userInfoUri, clientId);
	}
}
