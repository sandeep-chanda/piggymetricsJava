package com.piggymetrics.account.config;

import com.piggymetrics.account.service.security.CustomUserInfoTokenServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

@Configuration
public class OpaqueTokenConfig {

	@Bean
	public OpaqueTokenIntrospector opaqueTokenIntrospector(
			@Value("${spring.security.oauth2.resourceserver.opaque-token.introspection-uri:http://auth-service:5000/uaa/users/current}") String userInfoUri,
			@Value("${spring.security.oauth2.client.registration.account-service.client-id:account-service}") String clientId) {
		return new CustomUserInfoTokenServices(userInfoUri, clientId);
	}
}
