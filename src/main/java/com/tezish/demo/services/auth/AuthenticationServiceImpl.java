package com.tezish.demo.services.auth;

import com.tezish.demo.dto.request.LoginRequest;
import com.tezish.demo.dto.request.RegisterRequest;
import com.tezish.demo.dto.response.AuthenticationResponse;
import com.tezish.demo.dto.response.JWTTokenResponse;
import com.tezish.demo.enums.ERole;
import com.tezish.demo.model.User;
import com.tezish.demo.repository.UserRepository;
import com.tezish.demo.services.userDetails.CustomUserDetails;
import com.tezish.demo.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    //    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
//            TokenService tokenService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
//        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthenticationResponse registerUser(RegisterRequest request) throws BadRequestException {
        var existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(
                    "Password and Confirm Password do not match."
            );
        }

        var user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            user.setImageUrl(request.getImageUrl());
        }

        if (request.getRole() == null || request.getRole().isEmpty()) {
            user.setRole(ERole.ROLE_USER);
        } else {
            try {
                ERole role = ERole.valueOf(request.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
        }

        userRepository.save(user);
        UserDetails userDetails = CustomUserDetails.build(user);

//        JWTTokenResponse jwtTokenResponse = tokenService.saveUserTokensToRedis(userDetails);
        String jwtTokenResponse = jwtUtil.generateJwtToken(userDetails);

        return AuthenticationResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .imageUrl(user.getImageUrl())
                .accessToken(jwtTokenResponse)
//                .refreshToken(jwtTokenResponse.getRefreshToken())
                .build();
    }

    @Override
    public AuthenticationResponse authenticateUser(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + request.getEmail()));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = CustomUserDetails.build(user);
//            JWTTokenResponse jwtTokenResponse = tokenService.saveUserTokensToRedis(userDetails);
            String jwtTokenResponse = jwtUtil.generateJwtToken(userDetails);

            return AuthenticationResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .imageUrl(user.getImageUrl())
                    .accessToken(jwtTokenResponse)
//                    .refreshToken(jwtTokenResponse.getRefreshToken())
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getEmail(), e.getMessage());
            throw new UsernameNotFoundException(
                    "Authentication failed for user: " + request.getEmail() + ". Please check your credentials."
            );
        }
    }


    @Override
    public boolean logout(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        try {
            UserDetails userDetails = CustomUserDetails.build(user);
//            tokenService.revokeAllUserTokensInRedis(userDetails);
            SecurityContextHolder.clearContext();
            return true;
        } catch (Exception e) {
            log.error("Logout failed for user {}: {}", email, e.getMessage());
            return false;
        }
    }
}
