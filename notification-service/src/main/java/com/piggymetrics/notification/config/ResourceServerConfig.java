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
