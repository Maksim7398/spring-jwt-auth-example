package com.example.springjwtauthexample.security;

import com.example.springjwtauthexample.entity.RefreshToken;
import com.example.springjwtauthexample.entity.User;
import com.example.springjwtauthexample.exception.RefreshTokenException;
import com.example.springjwtauthexample.repository.UserRepository;
import com.example.springjwtauthexample.security.jwt.JwtUtils;
import com.example.springjwtauthexample.service.RefreshTokenService;
import com.example.springjwtauthexample.web.model.AuthResponse;
import com.example.springjwtauthexample.web.model.CreateUSerRequest;
import com.example.springjwtauthexample.web.model.LoginRequest;
import com.example.springjwtauthexample.web.model.RefreshTokenRequest;
import com.example.springjwtauthexample.web.model.RefreshTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final RefreshTokenService refreshTokenService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        AppUserDetails userDetails = (AppUserDetails) authenticate.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return AuthResponse.builder()
                .id(userDetails.getId())
                .token(jwtUtils.generateJwtToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    public void register(CreateUSerRequest createUSerRequest) {
        User user = User.builder()
                .username(createUSerRequest.getUsername())
                .email(createUSerRequest.getEmail())
                .password(passwordEncoder.encode(createUSerRequest.getPassword()))
                .roles(createUSerRequest.getRoles())
                .build();
        userRepository.save(user);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        return refreshTokenService.findByRefreshToken(refreshToken)
                .map(refreshTokenService::checkedRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User tokenOwner = userRepository
                            .findById(userId)
                            .orElseThrow(() ->
                                    new RefreshTokenException("Exception trying to get token for userId: " + userId));

                    String token = jwtUtils.generateTokenFromUserName(tokenOwner.getUsername());

                    return new RefreshTokenResponse(token, refreshTokenService.createRefreshToken(userId).getToken());
                }).orElseThrow(() -> new RefreshTokenException(refreshToken, "Refresh token notFound"));
    }

    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (currentPrincipal instanceof AppUserDetails userDetails) {
            Long id = userDetails.getId();
            refreshTokenService.deleteByUserId(id);
        }
    }
}
