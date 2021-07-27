package com.psl.supportportal.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.psl.supportportal.constant.FileConstant.*;
import com.psl.supportportal.constant.SecurityConstant;
import com.psl.supportportal.domain.HttpResponse;
import com.psl.supportportal.domain.User;
import com.psl.supportportal.domain.UserPrincipal;
import com.psl.supportportal.exception.domain.EmailAlreadyExistException;
import com.psl.supportportal.exception.domain.EmailNotFoundException;
import com.psl.supportportal.exception.domain.GlobalExceptionHandler;
import com.psl.supportportal.exception.domain.UserNotFoundException;
import com.psl.supportportal.exception.domain.UsernameExistException;
import com.psl.supportportal.service.UserService;
import com.psl.supportportal.utility.JwtTokenProvider;

@RestController
@RequestMapping(value = {"/","/user"})
public class UserResource extends GlobalExceptionHandler{
	
	private static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
	private static final String EMAIL_SENT = "An email with new password is sent to : ";
	private UserService userService;
	private AuthenticationManager authenticationManager;
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	public UserResource(UserService userService, AuthenticationManager authenticationManager,
			JwtTokenProvider jwtTokenProvider) {
		super();
		this.userService = userService;
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	

	@PostMapping("/register")
	public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailAlreadyExistException, UsernameExistException, MessagingException {
		User newUser = userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
		return new ResponseEntity<>(newUser, HttpStatus.OK);
	}
	
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user){
		authenticate(user.getUsername(), user.getPassword());
		User loginUser = userService.findUserByUsername(user.getUsername());
		UserPrincipal userPrincipal = new UserPrincipal(loginUser);
		HttpHeaders jwtHeaders = getJwtHeader(userPrincipal);
		return new ResponseEntity<>(loginUser, jwtHeaders, HttpStatus.OK);
	}
	
	@PostMapping("/add")
	public ResponseEntity<User> addNewUser(@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam("username") String username,
			@RequestParam("email") String email,
			@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,
			@RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException{
		User newUser = userService.addNewUser(firstName, lastName, username, email, role,
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<User>(newUser, HttpStatus.OK);
	}
	
	@PostMapping("/update")
	public ResponseEntity<User> update(@RequestParam("currentUsername") String currentUsername,
			@RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName,
			@RequestParam("username") String username,
			@RequestParam("email") String email,
			@RequestParam("role") String role,
			@RequestParam("isActive") String isActive,
			@RequestParam("isNotLocked") String isNotLocked,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException{
		User updatedUser = userService.updateUser(currentUsername, firstName, lastName, username, email, role,
				Boolean.parseBoolean(isNotLocked), Boolean.parseBoolean(isActive), profileImage);
		return new ResponseEntity<>(updatedUser, HttpStatus.OK);
	}
	
	@GetMapping("/list")
	public ResponseEntity<List<User>> getAllUsers(){
		List<User> users = userService.getUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}
	
	@GetMapping("/find/{username}")
	public ResponseEntity<User> getUser(@PathVariable("username") String username){
		User user = userService.findUserByUsername(username);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@GetMapping("/resetPassword/{email}")
	public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException{
		userService.resetPassword(email);
		return response(HttpStatus.OK, EMAIL_SENT+email);
	}
	
	@DeleteMapping("/delete/{id}")
	@PreAuthorize("hasAnyAuthority('user:delete')")
	public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") Long id){
		userService.deleteUser(id);
		return response(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
	}
	
	@PostMapping("/updateProfileImage")
	public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username, @RequestParam(value = "profileImage") MultipartFile profileImage) throws EmailAlreadyExistException, UsernameExistException, UserNotFoundException, IOException{
		User user = userService.updateProfileImage(username, profileImage);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@GetMapping(path = "/image/{username}/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(USER_FOLDER + FORWARD_SLASH + fileName));
	}
	
	@GetMapping(path = "/image/{profile}/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
		URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL+username);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try(InputStream inputStream = url.openStream()) {
			int bytesRead;
			byte[] chunk = new byte[1024];
			while ((bytesRead = inputStream.read(chunk)) > 0) {
				byteArrayOutputStream.write(chunk, 0, bytesRead);
			}
		}
		return byteArrayOutputStream.toByteArray();
	}
	
	private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
		HttpResponse body = new HttpResponse(httpStatus.value(), httpStatus,
				httpStatus.getReasonPhrase().toUpperCase(), message);
		return new ResponseEntity<HttpResponse>(body, httpStatus);
	}


	private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(SecurityConstant.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
		return headers;
	}

	private void authenticate(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	}
	
}
