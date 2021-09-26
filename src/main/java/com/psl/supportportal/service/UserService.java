package com.psl.supportportal.service;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.web.multipart.MultipartFile;

import com.psl.supportportal.domain.User;
import com.psl.supportportal.exception.domain.EmailAlreadyExistException;
import com.psl.supportportal.exception.domain.EmailNotFoundException;
import com.psl.supportportal.exception.domain.UserNotFoundException;
import com.psl.supportportal.exception.domain.UsernameExistException;


public interface UserService {
	
	User register(String firstName, String lastName, String userName, String email) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, MessagingException;
	
	List<User> getUsers();
	
	User findUserByUsername(String userName);
	
	User findUserByEmail(String email);
	
	User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException;

	User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException;
	
	void deleteUser(Long id);
	
	void resetPassword(String email) throws EmailNotFoundException, MessagingException;
	
	User updateProfileImage(String username, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException;
}
