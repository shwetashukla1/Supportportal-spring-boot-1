package com.psl.supportportal.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	
	private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
	private static final int ATTEMPT_INCREMENT = 1;
	private LoadingCache<String, Integer> loggingAttemptCache;
	
	
	public LoginAttemptService() {
		loggingAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
				.maximumSize(100).build(new CacheLoader<String, Integer>() {
					public Integer load(String key) {
						return 0;
					}
				});
	}
	
	public void evictUserFromLoginAttemptCache(String username) {
		loggingAttemptCache.invalidate(username);
	}
	
	public void addUserToLoginAttemptCache(String username) {
		int attempts = 0;
			try {
				attempts = ATTEMPT_INCREMENT + loggingAttemptCache.get(username);
				loggingAttemptCache.put(username, attempts);
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	}
	
	public boolean hasExceededMaxAttempts(String username) {
		boolean flag = true;
		try {
			flag = loggingAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return flag;
	}

}
