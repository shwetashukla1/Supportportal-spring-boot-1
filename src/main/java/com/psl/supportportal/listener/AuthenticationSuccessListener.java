package com.psl.supportportal.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.psl.supportportal.domain.UserPrincipal;
import com.psl.supportportal.service.LoginAttemptService;

@Component
public class AuthenticationSuccessListener {
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
	@EventListener
	public void OnAuthenticationSuccessListener(AuthenticationSuccessEvent event) {
		Object principal = event.getAuthentication().getPrincipal();
		if(principal instanceof UserPrincipal) {
			UserPrincipal user = (UserPrincipal) event.getAuthentication().getPrincipal();
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
	}

}
