package com.example.springjwtauthexample.web.controller;

import com.example.springjwtauthexample.exception.AlreadyExistsException;
import com.example.springjwtauthexample.repository.UserRepository;
import com.example.springjwtauthexample.security.SecurityService;
import com.example.springjwtauthexample.web.model.AuthResponse;
import com.example.springjwtauthexample.web.model.CreateUSerRequest;
import com.example.springjwtauthexample.web.model.LoginRequest;
import com.example.springjwtauthexample.web.model.RefreshTokenRequest;
import com.example.springjwtauthexample.web.model.RefreshTokenResponse;
import com.example.springjwtauthexample.web.model.SimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    private final SecurityService securityService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(securityService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> registerUser(@RequestBody CreateUSerRequest uSerRequest) {
        if (userRepository.existsByUsername(uSerRequest.getUsername())) {
            throw new AlreadyExistsException("this username already exist");
        }

        if (userRepository.existsByEmail(uSerRequest.getEmail())) {
            throw new AlreadyExistsException("this email already exist");
        }

        securityService.register(uSerRequest);

        return ResponseEntity.ok(new SimpleResponse("user created😊"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshTokenResponseResponseEntity(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(securityService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<SimpleResponse> logoutUser(@AuthenticationPrincipal UserDetails userDetails){
        securityService.logout();

        return ResponseEntity.ok(new SimpleResponse("User logout, username is: " + userDetails.getUsername()));
    }
}
