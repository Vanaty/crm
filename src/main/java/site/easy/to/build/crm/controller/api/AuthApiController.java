package site.easy.to.build.crm.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.config.CrmUserDetails;
import site.easy.to.build.crm.entity.ApiToken;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.ApiTokenRepository;
import site.easy.to.build.crm.repository.UserRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final CrmUserDetails userDetailsService;
    private final ApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthApiController(AuthenticationManager authenticationManager,
                           CrmUserDetails userDetailsService,
                           ApiTokenRepository apiTokenRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.apiTokenRepository = apiTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> request) {
        final String username = request.get("username");
        final String password = request.get("password");

        System.out.println("\n\n\nLogin"+username);

        try {
            User user = userRepository.findByUsername(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("status","error","message", "Invalid credentials"));
            }
            
            ApiToken apiToken = new ApiToken(user);
            apiTokenRepository.save(apiToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return ResponseEntity.ok(Map.of(
                "status","success",
                "token", apiToken.getToken(),
                "username", username,
                "roles", userDetails.getAuthorities(),
                "expiresAt", apiToken.getExpiresAt()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status","error","message", "Invalid credentials"));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> createGoogleAuthenticationToken(@RequestBody Map<String, String> request) {
        final String email = request.get("email");
        System.out.println("#\n\n\n"+email);
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Valid email is required"
            ));
        }
        final String username = email.split("@")[0];
        if (username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Username cannot be empty"
            ));
        }
        final String googleId = request.get("google_id"); // Consider validating or using this

        try {
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.ok(Map.of("status","error","message", "Invalid credentials"));
            } else if(!user.getRoles().get(0).getName().equals("ROLE_MANAGER")) {
                return ResponseEntity.ok(Map.of("status","error","message", "Invalid credentials"));
            }

            ApiToken apiToken = new ApiToken(user);
            apiTokenRepository.save(apiToken);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", apiToken.getToken(),
                "username", username,
                "roles",user.getRoles().get(0).getName(),
                "expiresAt", apiToken.getExpiresAt()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Invalid credentials: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            apiTokenRepository.findByTokenAndRevokedFalse(token)
                .ifPresent(apiToken -> {
                    apiToken.setRevoked(true);
                    apiTokenRepository.save(apiToken);
                });
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
