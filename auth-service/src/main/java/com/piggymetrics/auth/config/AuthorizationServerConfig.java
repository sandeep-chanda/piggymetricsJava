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
public class AuthorizationServerConfig {

	private final Environment env;

	public AuthorizationServerConfig(Environment env) {
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
