package com.psl.supportportal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.mail.MessagingException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.*;

import com.psl.supportportal.domain.User;
import com.psl.supportportal.domain.UserPrincipal;
import com.psl.supportportal.exception.domain.EmailAlreadyExistException;
import com.psl.supportportal.exception.domain.NotAnImageFileException;
import com.psl.supportportal.exception.domain.UserNotFoundException;
import com.psl.supportportal.exception.domain.UsernameExistException;
import com.psl.supportportal.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private BCryptPasswordEncoder passwordEncoder;
	@Mock
	private LoginAttemptService loginAttemptService;
	
	@InjectMocks
	private UserServiceImpl underTest;
	

	@Test
	void whenNoUserFoundTestLoadUserByUsername() {
		
		Assertions.assertThrows(UsernameNotFoundException.class, ()->{
			//given
			String username = "";
			//when
			underTest.loadUserByUsername(username);
		});
	}
	
	@Test()
	void whenUserFoundTestLoadUserByUsername() {
		//given
		String username = "shweta";
		User user = new User();
		user.setUsername(username);
		Mockito.when(userRepository.findUserByUsername(username)).thenReturn(user);
		//when
		UserPrincipal userPrincipal = (UserPrincipal) underTest.loadUserByUsername(username);
		//then
		User userLoaded = userPrincipal.getUser();
		user.setLastLoginDateDisplay(user.getLastLoginDate());
		user.setLastLoginDate(new Date());
		ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(argumentCaptor.capture());
		User userCaptured = argumentCaptor.getValue();
		assertThat(userLoaded).isEqualTo(userCaptured);
	}

	@Test
	void testRegister()
			throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, MessagingException {
		// given
		String firstName = "Shweta", lastName = "Shukla", userName = "shweta", email = "shweta@gmail.com";
		// when
		User user = underTest.register(firstName, lastName, userName, email);
		// then
		ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(argumentCaptor.capture());
		User capturedUser = argumentCaptor.getValue();
		assertThat(capturedUser).isEqualTo(user);
	}

	@Test
	void canGetUsers() {
		// when
		underTest.getUsers();
		// then
		verify(userRepository).findAll();
	}

	@Test()
	void testFindUserByUsername() {
		User user = new User();
		String userName = "shweta";
		user.setUsername(userName);
		
		Mockito.when(userRepository.findUserByUsername(userName)).thenReturn(user);
		User foundUser = underTest.findUserByUsername(userName);
		assertThat(foundUser.getUsername()).isEqualTo(user.getUsername());
	}

	@Test()
	void testFindUserByEmail() {
		User user = new User();
		String email = "shweta@gmail.com";
		user.setUsername(email);
		
		Mockito.when(userRepository.findUserByUsername(email)).thenReturn(user);
		User foundUser = underTest.findUserByUsername(email);
		assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	void testAddNewUser() throws IOException, EmailAlreadyExistException, UsernameExistException, UserNotFoundException,
			NotAnImageFileException {
		// given
		String firstName = "Shweta", lastName = "Shukla", userName = "shweta", email = "shweta@gmail.com",
				role = "ROLE_USER";
		boolean isNotLocked = true, isActive = true;
		File file = new File("src/test/resources/sampleimage.jpg");
		FileInputStream fileInputStream = new FileInputStream(file);
		MultipartFile profileImage = new MockMultipartFile("file", file.getName(), IMAGE_JPEG_VALUE,
				IOUtils.toByteArray(fileInputStream));
		// when
		User user = underTest.addNewUser(firstName, lastName, userName, email, role, isNotLocked, isActive,
				profileImage);
		// then
		ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(argumentCaptor.capture());
		User capturedUser = argumentCaptor.getValue();
		assertThat(capturedUser).isEqualTo(user);
	}

	@Test()
	void testUpdateUser() throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException,
			NotAnImageFileException, MessagingException {
		// given
		String firstName = "Shweta", lastName = "Shukla", userName = "shweta", email = "shweta@gmail.com",
				role = "ROLE_USER";
		boolean isNotLocked = true, isActive = true;
		File file = new File("src/test/resources/sampleimage.jpg");
		FileInputStream fileInputStream = new FileInputStream(file);
		MultipartFile profileImage = new MockMultipartFile("file", file.getName(), IMAGE_JPEG_VALUE,
				IOUtils.toByteArray(fileInputStream));
		User user = new User();
		user.setId(1L);
		user.setUsername(userName);
		Mockito.when(userRepository.findUserByUsername(userName)).thenReturn(user);
		User user1 = underTest.updateUser(userName, firstName, lastName, "shweta", email, role, isNotLocked,
				isActive, profileImage);
		
		verify(userRepository).save(user1);
	}

	@Test()
	void testDeleteUser() throws IOException {
		User user = new User();
		String username = "shweta";
		user.setUsername(username);
		user.setId(1L);
		Mockito.when(userRepository.findUserByUsername(username)).thenReturn(user);
		underTest.deleteUser(username);
		verify(userRepository).deleteById(user.getId());
	}

}