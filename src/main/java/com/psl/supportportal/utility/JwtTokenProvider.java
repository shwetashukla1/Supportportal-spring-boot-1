package com.psl.supportportal.utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import static com.auth0.jwt.algorithms.Algorithm.*;
import static com.psl.supportportal.constant.SecurityConstant.*;

import com.psl.supportportal.domain.UserPrincipal;

@Controller
public class JwtTokenProvider {
	
	@Value("${jwt.secret}")
	private String secret;
	
	public String generateJwtToken(UserPrincipal userPrincipal) {
		String claims[] = this.getClaimsFromUser(userPrincipal);
		return JWT.create()
				.withIssuer(PERSISTENT_SYSTEMS_LIMITED)
				.withAudience(PERSISTENT_SYSTEMS_LIMITED_ADMIN)
				.withIssuedAt(new Date())
				.withSubject(userPrincipal.getUsername())
				.withArrayClaim(AUTHORITIES, claims)
				.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.sign(HMAC512(secret.getBytes()));
	}

	private String[] getClaimsFromUser(UserPrincipal userPrincipal) {
		List<String> authorities = new ArrayList<>();
		for(GrantedAuthority authority : userPrincipal.getAuthorities()) {
			authorities.add(authority.getAuthority());
		}
		return authorities.toArray(new String[0]);
	}
	
	public List<GrantedAuthority> getAuthorities(String token) {
		String[] claims = this.getClaimsFromToken(token);
		return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}
	
	private String[] getClaimsFromToken(String token) {
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
	}

	private JWTVerifier getJWTVerifier() {
		JWTVerifier verifier;
		try {
			Algorithm algorithm = HMAC512(secret);
			verifier = JWT.require(algorithm).withIssuer(PERSISTENT_SYSTEMS_LIMITED).build();
		} catch (JWTVerificationException e) {
			throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
		}
		return verifier;
	}
	
	public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
				new UsernamePasswordAuthenticationToken(username, null, authorities);
		usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		
		return usernamePasswordAuthenticationToken;
	}

	public boolean isTokenValid(String userName, String token) {
		JWTVerifier verifier = getJWTVerifier();
		return StringUtils.isNoneEmpty(userName) && !isTokenExpired(verifier, token);
	}

	private boolean isTokenExpired(JWTVerifier verifier, String token) {
		Date expiration = verifier.verify(token).getExpiresAt();
		return expiration.before(new Date());
	}
	
	public String getSubject(String token) {
		JWTVerifier verifier = getJWTVerifier();
		return verifier.verify(token).getSubject();
	}
}
