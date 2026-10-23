package com.piggymetrics.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OAuth2AuthorizationConfig {
 @Bean @Order(1) SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception { OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http); http.getConfigurer(org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults()); return http.build(); }
 @Bean RegisteredClientRepository registeredClientRepository(Environment env) {
   RegisteredClient browser=RegisteredClient.withId(UUID.randomUUID().toString()).clientId("browser").clientAuthenticationMethod(ClientAuthenticationMethod.NONE).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("http://localhost:4000/oauth-callback.html").scope("ui").clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build()).build();
   return new InMemoryRegisteredClientRepository(browser, service("account-service",env.getProperty("ACCOUNT_SERVICE_PASSWORD","secret")), service("statistics-service",env.getProperty("STATISTICS_SERVICE_PASSWORD","secret")), service("notification-service",env.getProperty("NOTIFICATION_SERVICE_PASSWORD","secret")));
 }
 private RegisteredClient service(String id,String secret){ return RegisteredClient.withId(UUID.randomUUID().toString()).clientId(id).clientSecret("{noop}"+secret).clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC).authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).scope("server").build(); }
 @Bean JWKSource<SecurityContext> jwkSource() throws Exception { KeyPairGenerator g=KeyPairGenerator.getInstance("RSA"); g.initialize(2048); KeyPair p=g.generateKeyPair(); RSAKey key=new RSAKey.Builder((RSAPublicKey)p.getPublic()).privateKey((RSAPrivateKey)p.getPrivate()).keyID(UUID.randomUUID().toString()).build(); JWKSet set=new JWKSet(key); return (selector,context)->selector.select(set); }
 @Bean JwtDecoder jwtDecoder(JWKSource<SecurityContext> source){ return OAuth2AuthorizationServerConfiguration.jwtDecoder(source); }
 @Bean AuthorizationServerSettings authorizationServerSettings(){ return AuthorizationServerSettings.builder().issuer("http://auth-service:5000/uaa").authorizationEndpoint("/uaa/oauth2/authorize").tokenEndpoint("/uaa/oauth2/token").jwkSetEndpoint("/uaa/oauth2/jwks").build(); }
}
