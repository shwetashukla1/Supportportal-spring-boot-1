package com.psl.supportportal.exception.domain;

import java.io.IOException;
import java.util.Objects;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.psl.supportportal.domain.HttpResponse;

@RestControllerAdvice
public class GlobalExceptionHandler implements ErrorController{

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private static final String ACCOUNT_LOCKED = "Your Account has been locked please contact Administration";
	private static final String METHOD_IS_NOT_ALLOWED = "This request method is not allowed on this endpoint. Please send a '%s' request";
	private static final String INTERNAL_SERVER_ERROR_MESSAGE = "An error occurred while processing the request";
	private static final String INCORRECT_CREDENTIALS = "Username/Password is incorrect. Please Try again.";
	private static final String ACCOUNT_DISABLED = "Your Account has been disabled, if this is an error please contact administration";
	private static final String ERROR_PROCESSING_FILE = "Error occurred while processing file";
	private static final String NOT_ENOUGH_PERMISSION = "You do not have enough permission";
	private static final String ERROR_PATH = "/error";
	
	
	private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message){
		return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message.toUpperCase()),
				httpStatus);
	}
	
	@ExceptionHandler(DisabledException.class)
	public ResponseEntity<HttpResponse> accountDisabledException(){
		return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<HttpResponse> badCredentialsException(){
		return createHttpResponse(HttpStatus.BAD_REQUEST, INCORRECT_CREDENTIALS);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<HttpResponse> acccessDeniedException(){
		return createHttpResponse(HttpStatus.FORBIDDEN, NOT_ENOUGH_PERMISSION);
	}
	
	@ExceptionHandler(LockedException.class)
	public ResponseEntity<HttpResponse> lockedException(){
		return createHttpResponse(HttpStatus.UNAUTHORIZED, ACCOUNT_LOCKED);
	}
	
	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<HttpResponse> tokenExpiredException(TokenExpiredException exception){
		return createHttpResponse(HttpStatus.UNAUTHORIZED, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(EmailAlreadyExistException.class)
	public ResponseEntity<HttpResponse> emailAlreadyExistException(EmailAlreadyExistException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(UsernameExistException.class)
	public ResponseEntity<HttpResponse> usernameExistException(UsernameExistException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(EmailNotFoundException.class)
	public ResponseEntity<HttpResponse> emailNotFoundException(EmailNotFoundException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<HttpResponse> userNotFoundException(UserNotFoundException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage().toUpperCase());
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<HttpResponse> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception){
		HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods().iterator().next());
		return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<HttpResponse> methodNotSupportedException(NoHandlerFoundException exception){
		return createHttpResponse(HttpStatus.BAD_REQUEST, "Page Not Found");
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception){
		LOGGER.error(exception.getMessage());
		return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
	}
	
	@ExceptionHandler(NoResultException.class)
	public ResponseEntity<HttpResponse> notFoundException(NoResultException exception){
		LOGGER.error(exception.getMessage());
		return createHttpResponse(HttpStatus.NOT_FOUND, exception.getMessage());
	}
	
	@ExceptionHandler(IOException.class)
	public ResponseEntity<HttpResponse> iOException(IOException exception){
		LOGGER.error(exception.getMessage());
		return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PROCESSING_FILE);
	}
	
	@RequestMapping(ERROR_PATH)
	public ResponseEntity<HttpResponse> notFound404(){
		return createHttpResponse(HttpStatus.NOT_FOUND, "Page not found");
	}
	
	public String getErrorPath() {
		return ERROR_PATH;
	}
	
}
