package com.piggymetrics.statistics.service.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Opaque-token introspector that loads authentication from the auth-service user-info endpoint.
 */
public class CustomUserInfoTokenServices implements OpaqueTokenIntrospector {

	protected final Log logger = LogFactory.getLog(getClass());

	private static final String[] PRINCIPAL_KEYS = new String[] { "user", "username",
			"userid", "user_id", "login", "id", "name" };

	private final String userInfoEndpointUrl;

	private final String clientId;

	private final RestTemplate restTemplate = new RestTemplate();

	public CustomUserInfoTokenServices(String userInfoEndpointUrl, String clientId) {
		this.userInfoEndpointUrl = userInfoEndpointUrl;
		this.clientId = clientId;
	}

	@Override
	public OAuth2AuthenticatedPrincipal introspect(String token) {
		Map<String, Object> map = getMap(this.userInfoEndpointUrl, token);
		if (map.containsKey("error")) {
			this.logger.debug("userinfo returned error: " + map.get("error"));
			throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token"), "Invalid token");
		}
		return extractPrincipal(map);
	}

	private OAuth2AuthenticatedPrincipal extractPrincipal(Map<String, Object> map) {
		String name = String.valueOf(getPrincipal(map));
		Collection<GrantedAuthority> authorities = extractAuthorities(map);
		return new DefaultOAuth2AuthenticatedPrincipal(name, map, authorities);
	}

	@SuppressWarnings("unchecked")
	private Collection<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
		Set<String> scopes = new LinkedHashSet<>();
		Object request = map.get("oauth2Request");
		if (request instanceof Map) {
			Object scope = ((Map<String, Object>) request).get("scope");
			if (scope instanceof Collection) {
				for (Object s : (Collection<?>) scope) {
					scopes.add(String.valueOf(s));
				}
			} else if (scope instanceof String) {
				scopes.add((String) scope);
			}
		}
		Object scopeAttr = map.get("scope");
		if (scopeAttr instanceof Collection) {
			for (Object s : (Collection<?>) scopeAttr) {
				scopes.add(String.valueOf(s));
			}
		} else if (scopeAttr instanceof String) {
			for (String s : ((String) scopeAttr).split("\\s+")) {
				if (!s.isEmpty()) {
					scopes.add(s);
				}
			}
		}
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String scope : scopes) {
			authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
		}
		if (authorities.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("SCOPE_server"));
		}
		return authorities;
	}

	private Object getPrincipal(Map<String, Object> map) {
		for (String key : PRINCIPAL_KEYS) {
			if (map.containsKey(key)) {
				return map.get(key);
			}
		}
		Object nested = map.get("principal");
		if (nested instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> principalMap = (Map<String, Object>) nested;
			for (String key : PRINCIPAL_KEYS) {
				if (principalMap.containsKey(key)) {
					return principalMap.get(key);
				}
			}
		}
		if (map.containsKey("name")) {
			return map.get("name");
		}
		return this.clientId != null ? this.clientId : "unknown";
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> getMap(String path, String accessToken) {
		this.logger.debug("Getting user info from: " + path);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			ResponseEntity<Map> response = this.restTemplate.exchange(path, HttpMethod.GET,
					new HttpEntity<>(headers), Map.class);
			Map<String, Object> body = response.getBody();
			return body != null ? body : Collections.<String, Object>emptyMap();
		}
		catch (Exception ex) {
			this.logger.info("Could not fetch user details: " + ex.getClass() + ", "
					+ ex.getMessage());
			return Collections.<String, Object>singletonMap("error",
					"Could not fetch user details");
		}
	}
}
