package com.psl.supportportal.filter;

import static com.psl.supportportal.constant.SecurityConstant.ACCESS_DENIED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psl.supportportal.domain.HttpResponse;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		HttpResponse httpResponse = new HttpResponse(
				UNAUTHORIZED.value(),
				UNAUTHORIZED, 
				UNAUTHORIZED.getReasonPhrase(),
				ACCESS_DENIED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(UNAUTHORIZED.value());
		OutputStream outputStream = response.getOutputStream();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(outputStream, httpResponse);
		outputStream.flush();
	}

}
