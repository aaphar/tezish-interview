package com.tezish.demo.services.token;

import com.tezish.demo.dto.response.JWTTokenResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface TokenService {

    JWTTokenResponse saveUserTokensToRedis(UserDetails user);

    void revokeAllUserTokensInRedis(UserDetails user);

    Map<String, String> getUserTokensFromRedis(UserDetails user);

}
