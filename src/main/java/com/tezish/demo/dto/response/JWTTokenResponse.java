package com.tezish.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JWTTokenResponse {
    private String accessToken;
    private String refreshToken;
}