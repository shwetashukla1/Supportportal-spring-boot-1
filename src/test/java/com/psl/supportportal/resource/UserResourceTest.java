package com.psl.supportportal.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psl.supportportal.domain.User;
import com.psl.supportportal.domain.UserPrincipal;
import com.psl.supportportal.filter.JwtAccessDeniedHandler;
import com.psl.supportportal.filter.JwtAuthenticationEntryPoint;
import com.psl.supportportal.service.UserService;
import com.psl.supportportal.utility.JwtTokenProvider;

@WebMvcTest(UserResource.class)
class UserResourceTest {
	
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@MockBean 
	private UserService userService;
	@MockBean
	private JwtTokenProvider jwtTokenProvider;
	@MockBean
	private JwtAccessDeniedHandler jwtAccessDeniedHandler;
	@MockBean
	private AuthenticationManager authenticationManager;
	
	@InjectMocks UserResource userResource;
	@MockBean JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	@MockBean UserDetailsService userDetailsService;
	
	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void testRegister() throws JsonProcessingException, Exception {
		String firstName="Shweta", lastName="Shukla", username="shweta", email="shweta@gmail.com";
		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setUsername(username);
		user.setEmail(email);
		Mockito.when(userService.register(firstName, lastName, username, email)).thenReturn(user);
		
		ObjectMapper mapper = new ObjectMapper();
		
		mockMvc.perform(post("/user/register")
				.content(mapper.writeValueAsString(user))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(username)))
				.andExpect(jsonPath("$.firstName", is(firstName)))
				.andExpect(jsonPath("$.lastName", is(lastName)))
				.andExpect(jsonPath("$.email", is(email)));
	}

	@Test
	void testLogin() throws JsonProcessingException, Exception {
		String username = "shweta", password = "xyz";
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(token);
		Mockito.when(userService.findUserByUsername(user.getUsername())).thenReturn(user);
		UserPrincipal userPrincipal = new UserPrincipal(user);
		Mockito.when(jwtTokenProvider.generateJwtToken(userPrincipal)).thenReturn("");
		ObjectMapper mapper = new ObjectMapper();
		
		mockMvc.perform(post("/user/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is(username)))
				.andDo(print());
	}

	@Test
	void testAddNewUser() throws Exception {
		String firstName="Shweta", lastName="Shukla", username="shweta", email="shweta@gmail.com", role="ROLE_USER";
		String isActive = "true", isNotLocked = "true";
		User user = new User();
		user.setUsername(username);
		Mockito.when(userService.addNewUser(firstName, lastName, username, email, role, true, true, null)).thenReturn(user);
		mockMvc.perform(post("/user/add").contentType(MediaType.APPLICATION_JSON)
				.param("firstName", firstName)
				.param("lastName", lastName)
				.param("username", username)
				.param("email", email)
				.param("role", role)
				.param("isActive", isActive)
				.param("isNotLocked", isNotLocked))
				.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)));
	}

	@Test
	void testUpdate() throws Exception {
		String currentUsername="shweta", firstName="Shweta", lastName="Shukla", username="shweta", email="shweta@gmail.com", role="ROLE_USER";
		String isActive = "true", isNotLocked = "true";
		User user = new User();
		user.setUsername(username);
		Mockito.when(userService.updateUser(currentUsername, firstName, lastName, username, email, role, true, true, null)).thenReturn(user);
		mockMvc.perform(post("/user/update").contentType(MediaType.APPLICATION_JSON)
				.param("currentUsername", currentUsername)
				.param("firstName", firstName)
				.param("lastName", lastName)
				.param("username", username)
				.param("email", email)
				.param("role", role)
				.param("isActive", isActive)
				.param("isNotLocked", isNotLocked))
				.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(username)));
	}

	@Test
	void testGetAllUsers() throws Exception {
		User user = new User();
		user.setUsername("shweta");
		List<User> users = Arrays.asList(user);
		Mockito.when(userService.getUsers()).thenReturn(users);
		mockMvc.perform(get("/user/list").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].username", is(user.getUsername())));
	}

	@Test
	void testGetUser() throws Exception {
		User user = new User();
		user.setUsername("shweta");
		user.setFirstName("Shweta");
		Mockito.when(userService.findUserByUsername(user.getUsername())).thenReturn(user);
		mockMvc.perform(get("/user/find/"+user.getUsername()).contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value(user.getFirstName()));
	}

	@Test
	@WithMockUser( authorities = { "user:delete" })
	void testDeleteUser() throws Exception {
		User user = new User();
		user.setUsername("xyz");
		Mockito.doNothing().when(userService).deleteUser(user.getUsername());
		mockMvc.perform(delete("/user/delete/"+user.getUsername())
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
}
