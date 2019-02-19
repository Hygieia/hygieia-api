package com.capitalone.dashboard.auth.token;

import com.capitalone.dashboard.auth.AuthProperties;
import com.capitalone.dashboard.auth.AuthenticationFixture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenAuthenticationServiceImplTest {

	private static final String USERNAME = "username";
	private static final String AUTHORIZATION = "Authorization";
	private static final String AUTH_PREFIX_W_SPACE = "Bearer ";
	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	
	private TokenAuthenticationServiceImpl service;
	
	private AuthProperties tokenAuthProperties;
	
	@Mock
	private HttpServletResponse response;
	
	@Mock
	private HttpServletRequest request;
	
	@Before
	public void setup() {
		tokenAuthProperties = new AuthProperties();
		tokenAuthProperties.setExpirationTime(100000L);
		tokenAuthProperties.setSecret("somesupersecretphrase");
		service = new TokenAuthenticationServiceImpl(tokenAuthProperties);
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public void testAddAuthentication() {
		service.addAuthentication(response, AuthenticationFixture.getAuthentication(USERNAME));
		verify(response).addHeader(eq(AUTH_RESPONSE_HEADER), anyString());
	}
	
	@Test
	public void testGetAuthentication_noHeader() {
		when(request.getHeader(AUTHORIZATION)).thenReturn(null);
		assertNull(service.getAuthentication(request));
	}
	
	@Test
	public void testGetAuthentication_expiredToken() {
		when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_PREFIX_W_SPACE + AuthenticationFixture.getJwtToken(USERNAME, tokenAuthProperties.getSecret(), 0));
		assertNull(service.getAuthentication(request));
	}
	
	@Test
	public void testGetAuthentication() {
		when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_PREFIX_W_SPACE + AuthenticationFixture.getJwtToken(USERNAME, tokenAuthProperties.getSecret(), tokenAuthProperties.getExpirationTime()));
		Authentication result = service.getAuthentication(request);
		
		assertNotNull(result);
		assertEquals(USERNAME, result.getName());
		assertEquals(2, result.getAuthorities().size());
		assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
		assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
		
		assertNotNull(result.getDetails());
		
		verify(request).getHeader(AUTHORIZATION);
	}

}
