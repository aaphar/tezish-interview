package com.tezish.demo.controller;

import com.tezish.demo.dto.request.LoginRequest;
import com.tezish.demo.dto.request.RegisterRequest;
import com.tezish.demo.dto.response.AuthenticationResponse;
import com.tezish.demo.services.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) throws BadRequestException {
        return ResponseEntity.ok(authenticationService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticateUser(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestParam String email) {
        return ResponseEntity.ok(authenticationService.logout(email));
    }

}
