package com.psl.supportportal.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import static com.psl.supportportal.constant.SecurityConstant.FORBIDDEN_MESSAGE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psl.supportportal.domain.HttpResponse;

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
			throws IOException {
		HttpResponse httpResponse = new HttpResponse(
				FORBIDDEN.value(),
				FORBIDDEN, 
				FORBIDDEN.getReasonPhrase().toUpperCase(),
				FORBIDDEN_MESSAGE);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(FORBIDDEN.value());
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
	}

}
