package com.tezish.demo.services.auth;

import com.tezish.demo.dto.request.LoginRequest;
import com.tezish.demo.dto.request.RegisterRequest;
import com.tezish.demo.dto.response.AuthenticationResponse;
import org.apache.coyote.BadRequestException;

public interface AuthenticationService {

    AuthenticationResponse registerUser(RegisterRequest registerRequest) throws BadRequestException;

    AuthenticationResponse authenticateUser(LoginRequest loginRequest);

    boolean logout(String email);

}
