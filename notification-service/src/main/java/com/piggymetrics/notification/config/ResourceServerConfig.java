package com.piggymetrics.notification.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author cdov
 */
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

	@Bean
	public SecurityFilterChain notificationSecurityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(Customizer.withDefaults()))
				.csrf(csrf -> csrf.disable());
		return http.build();
	}

	@Bean
	public OpaqueTokenIntrospector opaqueTokenIntrospector(
			@Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://auth-service:5000/uaa/users/current}") String userInfoUri,
			@Value("${spring.security.oauth2.client.registration.notification-service.client-id:notification-service}") String clientId) {
		RestTemplate restTemplate = new RestTemplate();
		return token -> {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setBearerAuth(token);
				ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET,
						new HttpEntity<>(headers), Map.class);
				Map<String, Object> body = response.getBody() != null ? response.getBody() : Collections.emptyMap();
				String name = body.containsKey("name") ? String.valueOf(body.get("name")) : clientId;
				return new DefaultOAuth2AuthenticatedPrincipal(name, body,
						List.of(new SimpleGrantedAuthority("SCOPE_server")));
			}
			catch (Exception ex) {
				throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Invalid token", ex);
			}
		};
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean
	public OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientService authorizedClientService) {
		OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
				.clientCredentials()
				.build();
		AuthorizedClientServiceOAuth2AuthorizedClientManager manager =
				new AuthorizedClientServiceOAuth2AuthorizedClientManager(
						clientRegistrationRepository, authorizedClientService);
		manager.setAuthorizedClientProvider(authorizedClientProvider);
		return manager;
	}

	@Bean
	public RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
		return requestTemplate -> {
			try {
				OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
						.withClientRegistrationId("notification-service")
						.principal("notification-service")
						.build();
				OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);
				if (client != null && client.getAccessToken() != null) {
					requestTemplate.header(HttpHeaders.AUTHORIZATION,
							"Bearer " + client.getAccessToken().getTokenValue());
				}
			}
			catch (Exception ignored) {
				// Token endpoint may be unavailable in unit tests.
			}
		};
	}
}
