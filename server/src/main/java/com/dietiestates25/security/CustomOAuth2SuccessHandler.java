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

        // Extract attributes based on provider (safe fallback)
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String login = oAuth2User.getAttribute("login"); // GitHub specific

        // GitHub might not return email if private, use login or fallback
        if (email == null) {
            if (login != null) {
                email = login + "@github.com"; // Placeholder email for GitHub users without public email
            } else {
                email = "user_" + UUID.randomUUID().toString().substring(0, 8) + "@oauth.com";
            }
        }

        String firstName = null;
        String lastName = null;

        // Try standard OpenID attributes first
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");

        if (givenName != null)
            firstName = givenName;
        if (familyName != null)
            lastName = familyName;

        // If split names missing, try to parse full name
        if (firstName == null && name != null) {
            String[] parts = name.split(" ", 2);
            if (parts.length > 0)
                firstName = parts[0];
            if (parts.length > 1)
                lastName = parts[1];
        }

        // Fallback for GitHub (often just has 'name' or 'login')
        if (firstName == null && login != null) {
            firstName = login;
        }

        // Final safe defaults
        if (firstName == null)
            firstName = "User";
        if (lastName == null)
            lastName = "";

        final String finalFirstName = firstName;
        final String finalLastName = lastName;
        final String finalEmail = email;

        User user = userRepository.findByEmail(finalEmail).map(existingUser -> {
            // Update name if we have better info now
            boolean updated = false;
            // Only update if existing is generic/placeholder and we have something better
            if ((existingUser.getFirstName() == null || existingUser.getFirstName().equals("User"))
                    && !finalFirstName.equals("User")) {
                existingUser.setFirstName(finalFirstName);
                updated = true;
            }
            if ((existingUser.getLastName() == null || existingUser.getLastName().isEmpty())
                    && !finalLastName.isEmpty()) {
                existingUser.setLastName(finalLastName);
                updated = true;
            }
            if (updated) {
                return userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setFirstName(finalFirstName);
            newUser.setLastName(finalLastName);
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.getRoles().add(Role.USER);

            // Determine provider
            String registrationId = "OAUTH";
            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication)
                        .getAuthorizedClientRegistrationId().toUpperCase();
            }
            newUser.setAuthProvider(registrationId);

            return userRepository.save(newUser);
        });

        // Embed user info in JWT claims
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        // Also send authProvider to frontend so it can hide password field
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

        String token = jwtService.generateToken(extraClaims, userDetails);

        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth/login?token=" + token);
    }
}
