package com.mlm.backend.Controller;

import com.mlm.backend.Model.User;
import com.mlm.backend.Model.UserRole;
import com.mlm.backend.Repository.UserRepository;
import com.mlm.backend.Security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          JwtTokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        // 1. Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        // 2. Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Force default role (ENGINEER)
        user.setRole(UserRole.ENGINEER);

        // 4. Save to DB
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.get("email"), loginRequest.get("password"))
        );

        // Cast to your custom class
        com.mlm.backend.Security.CustomUserDetails userDetails =
                (com.mlm.backend.Security.CustomUserDetails) authentication.getPrincipal();

        // Extract authority correctly
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = tokenProvider.generateToken(userDetails.getUsername(), role);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", userDetails.getUsername(),
                "role", role
        ));
    }

}