package com.tezish.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticationResponse {

    private Long id;

    private String email;
    private String fullName;
    private String imageUrl;
    private String role;
    private String accessToken;
    private String refreshToken;
}
