package com.piggymetrics.account.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { http.authorizeHttpRequests(a->a.requestMatchers("/","/demo").permitAll().anyRequest().authenticated()).oauth2ResourceServer(o->o.jwt(Customizer.withDefaults())); return http.build(); }
 @Bean OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository registrations, OAuth2AuthorizedClientService clients) { OAuth2AuthorizedClientRepository repository=new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(clients); DefaultOAuth2AuthorizedClientManager manager=new DefaultOAuth2AuthorizedClientManager(registrations,repository); manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()); return manager; }
 @Bean RequestInterceptor oauth2FeignRequestInterceptor(OAuth2AuthorizedClientManager manager) { return template->{ OAuth2AuthorizeRequest request=OAuth2AuthorizeRequest.withClientRegistrationId("account-service").principal("account-service").build(); OAuth2AuthorizedClient client=manager.authorize(request); if(client!=null) template.header("Authorization","Bearer "+client.getAccessToken().getTokenValue()); }; }
}
