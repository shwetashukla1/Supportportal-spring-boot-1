package com.psl.supportportal.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.psl.supportportal.domain.User;
import com.psl.supportportal.enumeration.Role;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

	@Test
	void testWhenFindUserByUsername() {
		// given
		User user = new User();
		user.setUserId(RandomStringUtils.randomNumeric(10));
		user.setUsername("shweta");
		String password = RandomStringUtils.randomAlphanumeric(10);
		user.setPassword(passwordEncoder.encode(password));
		user.setActive(true);
		user.setEmail("shweta@gmail.com");
		user.setFirstName("Shweta");
		user.setLastName("Shukla");
		user.setJoinDate(new Date());
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		userRepository.save(user);

		// when
		User retrievedUser = userRepository.findUserByUsername("shweta");

		// then
		assertEquals(user, retrievedUser, "retreived user is as expected");
	}
	
	@Test
	void testWhenFindNoUserByUsername() {
		// when
		User retrievedUser = userRepository.findUserByUsername("shweta");

		// then
		assertEquals(null, retrievedUser, "No user found by username");
	}

	@Test
	void testWhenFindUserByEmail() {
		// given
		User user = new User();
		user.setUserId(RandomStringUtils.randomNumeric(10));
		user.setUsername("shweta");
		String password = RandomStringUtils.randomAlphanumeric(10);
		user.setPassword(passwordEncoder.encode(password));
		user.setActive(true);
		user.setEmail("shweta@gmail.com");
		user.setFirstName("Shweta");
		user.setLastName("Shukla");
		user.setJoinDate(new Date());
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		userRepository.save(user);

		// when
		User retrievedUser = userRepository.findUserByEmail("shweta@gmail.com");

		// then
		assertEquals(user, retrievedUser, "retreived user is as expected");
	}
	
	@Test
	void testWhenFindNoUserByEmail() {
		// when
		User retrievedUser = userRepository.findUserByEmail("shweta@gmail.com");

		// then
		assertEquals(null, retrievedUser, "No user found by email");
	}

}
