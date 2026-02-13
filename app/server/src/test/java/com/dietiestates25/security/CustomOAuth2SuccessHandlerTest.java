package com.dietiestates25.security;

import com.dietiestates25.config.JwtService;
import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2SuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private CustomOAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void onAuthenticationSuccess_NewUser_CreatesUserAndRedirects() throws IOException {
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0)); // Fix for
        // DefaultRedirectStrategy

        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        // Use lenient to avoid strict stubbing errors for attributes that might be
        // checked but not used in final result
        lenient().when(oAuth2User.getAttribute("email")).thenReturn("newuser@test.com");
        lenient().when(oAuth2User.getAttribute("name")).thenReturn("New User");
        lenient().when(oAuth2User.getAttribute("login")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("given_name")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("family_name")).thenReturn(null);

        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Mock save to return the user
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        when(jwtService.generateToken(anyMap(), any(UserDetails.class))).thenReturn("mockToken");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository).save(any(User.class));
        verify(response).sendRedirect("http://localhost:4200/auth/login?token=mockToken");
    }

    @Test
    void onAuthenticationSuccess_ExistingUser_UpdatesAndRedirects() throws IOException {
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        User existingUser = new User();
        existingUser.setEmail("existing@test.com");
        existingUser.setFirstName("User");
        existingUser.setLastName("");
        existingUser.setPasswordHash("hash");
        existingUser.getRoles().add(Role.USER);
        existingUser.setAuthProvider("OAUTH");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        lenient().when(oAuth2User.getAttribute("email")).thenReturn("existing@test.com");
        lenient().when(oAuth2User.getAttribute("given_name")).thenReturn("NewFirst");
        lenient().when(oAuth2User.getAttribute("family_name")).thenReturn("NewLast");
        lenient().when(oAuth2User.getAttribute("name")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("login")).thenReturn(null);

        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return
                                                                                                        // updated user

        when(jwtService.generateToken(anyMap(), any(UserDetails.class))).thenReturn("mockToken");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Verify update happened
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("NewFirst", userCaptor.getValue().getFirstName());
        assertEquals("NewLast", userCaptor.getValue().getLastName());

        verify(response).sendRedirect("http://localhost:4200/auth/login?token=mockToken");
    }

    @Test
    void onAuthenticationSuccess_GithubLogin_UsesLoginAsEmail() throws IOException {
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        lenient().when(oAuth2User.getAttribute("email")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("login")).thenReturn("githubuser");
        lenient().when(oAuth2User.getAttribute("name")).thenReturn("Github User");
        lenient().when(oAuth2User.getAttribute("given_name")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("family_name")).thenReturn(null);

        when(userRepository.findByEmail("githubuser@github.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); // Added mock
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyMap(), any(UserDetails.class))).thenReturn("mockToken");

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("githubuser@github.com", userCaptor.getValue().getEmail());
    }

    @Test
    void onAuthenticationSuccess_OAuthToken_SetsAuthProvider() throws IOException {
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        when(oauthToken.getPrincipal()).thenReturn(oAuth2User);
        when(oauthToken.getAuthorizedClientRegistrationId()).thenReturn("google");

        lenient().when(oAuth2User.getAttribute("email")).thenReturn("user@test.com");
        lenient().when(oAuth2User.getAttribute("name")).thenReturn("User");
        lenient().when(oAuth2User.getAttribute("login")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("given_name")).thenReturn(null);
        lenient().when(oAuth2User.getAttribute("family_name")).thenReturn(null);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword"); // Added mock
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyMap(), any(UserDetails.class))).thenReturn("mockToken");

        successHandler.onAuthenticationSuccess(request, response, oauthToken);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("GOOGLE", userCaptor.getValue().getAuthProvider());
    }
}
