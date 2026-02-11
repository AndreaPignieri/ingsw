package com.dietiestates25.security;

import com.dietiestates25.model.Role;
import com.dietiestates25.model.User;
import com.dietiestates25.repository.UserRepository;
import com.dietiestates25.config.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = extractEmail(oAuth2User);
        String[] names = extractNames(oAuth2User);
        String firstName = names[0];
        String lastName = names[1];

        User user = findOrCreateUser(email, firstName, lastName, authentication);
        String token = createToken(user);

        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth/login?token=" + token);
    }

    private String extractEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String login = oAuth2User.getAttribute("login");

        if (email != null) {
            return email;
        }
        if (login != null) {
            return login + "@github.com";
        }
        return "user_" + UUID.randomUUID().toString().substring(0, 8) + "@oauth.com";
    }

    private String[] extractNames(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        String login = oAuth2User.getAttribute("login");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        String firstName = givenName;
        String lastName = familyName;

        if (firstName == null && name != null) {
            String[] parts = name.split(" ", 2);
            if (parts.length > 0)
                firstName = parts[0];
            if (parts.length > 1)
                lastName = parts[1];
        }

        if (firstName == null && login != null) {
            firstName = login;
        }

        if (firstName == null)
            firstName = "User";
        if (lastName == null)
            lastName = "";

        return new String[] { firstName, lastName };
    }

    private User findOrCreateUser(String email, String firstName, String lastName, Authentication authentication) {
        return userRepository.findByEmail(email)
                .map(existingUser -> updateUserIfNeeded(existingUser, firstName, lastName))
                .orElseGet(() -> createNewUser(email, firstName, lastName, authentication));
    }

    private User updateUserIfNeeded(User existingUser, String firstName, String lastName) {
        boolean updated = false;
        if ((existingUser.getFirstName() == null || "User".equals(existingUser.getFirstName()))
                && !"User".equals(firstName)) {
            existingUser.setFirstName(firstName);
            updated = true;
        }
        if ((existingUser.getLastName() == null || existingUser.getLastName().isEmpty())
                && !lastName.isEmpty()) {
            existingUser.setLastName(lastName);
            updated = true;
        }
        return updated ? userRepository.save(existingUser) : existingUser;
    }

    private User createNewUser(String email, String firstName, String lastName, Authentication authentication) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.getRoles().add(Role.USER);

        String registrationId = "OAUTH";
        if (authentication instanceof OAuth2AuthenticationToken oauth2authenticationtoken) {
            registrationId = oauth2authenticationtoken.getAuthorizedClientRegistrationId().toUpperCase();
        }
        newUser.setAuthProvider(registrationId);

        return userRepository.save(newUser);
    }

    private String createToken(User user) {
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        extraClaims.put("authProvider", user.getAuthProvider());

        String role = user.getRoles().stream()
                .findFirst()
                .map(java.lang.Enum::name)
                .orElse("USER");
        extraClaims.put("role", role);

        var userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getRoles().stream()
                        .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.name()))
                        .collect(java.util.stream.Collectors.toList()));

        return jwtService.generateToken(extraClaims, userDetails);
    }
}
