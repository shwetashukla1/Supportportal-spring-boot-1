package com.psl.supportportal.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.psl.supportportal.constant.FileConstant.*;

import static com.psl.supportportal.constant.UserImplConstant.*;
import com.psl.supportportal.domain.User;
import com.psl.supportportal.domain.UserPrincipal;
import com.psl.supportportal.enumeration.Role;
import com.psl.supportportal.exception.domain.EmailAlreadyExistException;
import com.psl.supportportal.exception.domain.EmailNotFoundException;
import com.psl.supportportal.exception.domain.UserNotFoundException;
import com.psl.supportportal.exception.domain.UsernameExistException;
import com.psl.supportportal.repository.UserRepository;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
//	@Autowired
//	private EmailService emailService;


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findUserByUsername(username);
		if(user == null ) {
			logger.error(username);
			throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+username);
		}
		else {
			validateLoginAttempt(user);
			user.setLastLoginDateDisplay(user.getLastLoginDate());
			user.setLastLoginDate(new Date());
			userRepository.save(user);
			
			UserPrincipal userPrincipal= new UserPrincipal(user);
			logger.info(USER_FOUND_BY_USERNAME+username);
			
			return userPrincipal;
		}
	}

	private void validateLoginAttempt(User user) {
		if(user.isNotLocked()) {
			if(loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
				user.setNotLocked(false);
			}
			else {
				user.setNotLocked(true);
			}
		}
		else {
			loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
		}
		
	}

	@Override
	public User register(String firstName, String lastName, String userName, String email) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, MessagingException {
		validateNewUserNameAndEmail(StringUtils.EMPTY, userName, email);
		User user = new User();
		user.setUserId(generateUserId());
		user.setUsername(userName);
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		user.setActive(true);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setNotLocked(true);
		user.setRole(Role.ROLE_USER.name());
		user.setAuthorities(Role.ROLE_USER.getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(userName));
		userRepository.save(user);
		logger.info("New user password : "+ password);
		//emailService.sendPasswordEmail(firstName, password, email);
		return user;
	}

	private String getTemporaryProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
	}


	private String encodePassword(String password) {
		return passwordEncoder.encode(password);
	}


	private String generatePassword() {
		return RandomStringUtils.randomAlphanumeric(10);
	}

	private String generateUserId() {
		return RandomStringUtils.randomNumeric(10);
	}

	private User validateNewUserNameAndEmail(String currentUserName, String newUserName, String newEmail) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException {
		User userByNewUsername = findUserByUsername(newUserName);
		User userByNewEmail = findUserByEmail(newEmail);
		
		if(StringUtils.isNotBlank(currentUserName)) {
			User currentUser = findUserByUsername(currentUserName);
			if(currentUser == null) {
				throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME+currentUserName);
			}
			if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
				throw new EmailAlreadyExistException(EMAIL_ALREADY_EXISTS);
			}
			return currentUser;
		}
		else {
			if(userByNewUsername != null) {
				throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
			}
			if(userByNewEmail != null) {
				throw new EmailAlreadyExistException(EMAIL_ALREADY_EXISTS);
			}
			return null;
		}
	}

	@Override
	public List<User> getUsers() {
		return userRepository.findAll();
	}

	@Override
	public User findUserByUsername(String userName) {
		return userRepository.findUserByUsername(userName);
	}

	@Override
	public User findUserByEmail(String email) {
		return userRepository.findUserByEmail(email);
	}

	@Override
	public User addNewUser(String firstName, String lastName, String username, String email, String role,
			boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException {
		validateNewUserNameAndEmail(StringUtils.EMPTY, username, email);
		User user = new User();
		user.setUserId(generateUserId());
		user.setUsername(username);
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		user.setActive(isActive);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setJoinDate(new Date());
		user.setNotLocked(isNotLocked);
		user.setRole(getRoleEnumName(role).name());
		user.setAuthorities(getRoleEnumName(role).getAuthorities());
		user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
		userRepository.save(user);
		saveProfileImage(user, profileImage);
		return user;
	}

	private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {
		if(profileImage != null) {
			Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
			if(!Files.exists(userFolder)) {
				Files.createDirectories(userFolder);
				logger.info(DIRECTORY_CREATED + userFolder);
			}
			Files.deleteIfExists(Paths.get(USER_FOLDER + user.getUsername() +DOT+ JPG_EXTENSION));
			Files.copy(profileImage.getInputStream() , userFolder.resolve(user.getUsername() +DOT+JPG_EXTENSION));
			user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
			userRepository.save(user);
			logger.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
		}
		
	}

	private String setProfileImageUrl(String username) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH 
				+ username + DOT + JPG_EXTENSION).toUriString();		
	}

	private Role getRoleEnumName(String role) {
		return Role.valueOf(role.toUpperCase());
	}

	@Override
	public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
			String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException {
		User currentUser = validateNewUserNameAndEmail(currentUsername, newUsername, newEmail);
		currentUser.setUsername(newUsername);
		currentUser.setActive(isActive);
		currentUser.setEmail(newEmail);
		currentUser.setFirstName(newFirstName);
		currentUser.setLastName(newLastName);
		currentUser.setNotLocked(isNotLocked);
		currentUser.setRole(getRoleEnumName(role).name());
		currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
		userRepository.save(currentUser);
		saveProfileImage(currentUser, profileImage);
		return currentUser;
	}

	@Override
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}

	@Override
	public void resetPassword(String email) throws EmailNotFoundException {
		User user = userRepository.findUserByEmail(email);
		if(user == null) {
			throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL);
		}
		String password = generatePassword();
		user.setPassword(encodePassword(password));
		userRepository.save(user);
		//emailService.sendPasswordEmail(user.getFirstName(), password, email);
	}

	@Override
	public User updateProfileImage(String username, MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException {
		User user = validateNewUserNameAndEmail(username, null, null);
		saveProfileImage(user, profileImage);
		return user;
	}

}