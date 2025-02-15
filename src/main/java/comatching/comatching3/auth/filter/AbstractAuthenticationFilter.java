package comatching.comatching3.auth.filter;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import comatching.comatching3.auth.service.CustomDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	protected final AuthenticationManager authenticationManager;
	protected final CustomDetailsService customDetailsService;

	public AbstractAuthenticationFilter(AuthenticationManager authenticationManager, CustomDetailsService customDetailsService) {
		this.authenticationManager = authenticationManager;
		this.customDetailsService = customDetailsService;
		setFilterProcessesUrl(getLoginUrl());
	}

	protected Map<String, String> parseRequest(HttpServletRequest request) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(request.getInputStream(), Map.class);
	}

	protected abstract String getLoginUrl();

	protected String getUsernameKey(Map<String, String> creds) {
		return creds.get("accountId");

	}
	protected String getPasswordKey(Map<String, String> creds) {
		return creds.get("password");
	}
}
