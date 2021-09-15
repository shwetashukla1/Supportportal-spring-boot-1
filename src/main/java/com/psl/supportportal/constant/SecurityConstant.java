package com.psl.supportportal.constant;

public class SecurityConstant {
	
	public static final long EXPIRATION_TIME = 432_000_000;  //5 days in milliseconds
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String JWT_TOKEN_HEADER = "Jwt-Token";
	public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
	public static final String PERSISTENT_SYSTEMS_LIMITED = "Persistent Systems Limited"; // company providing token
	public static final String PERSISTENT_SYSTEMS_LIMITED_ADMIN = "User Management Portal"; //AUDIENCE OF THE TOKEN 
	public static final String AUTHORITIES = "Authorities";
	public static final String FORBIDDEN_MESSAGE = "You need to login to access this page";
	public static final String ACCESS_DENIED = "You don't have access to access this page";
	public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
	public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/image/**"};
	
}