package com.psl.supportportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psl.supportportal.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findUserByUsername(String username);
	User findUserByEmail(String email);
	
}
