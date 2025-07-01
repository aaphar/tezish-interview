package com.tezish.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class MessageResponse {
    private HttpStatus statusCode;
    private String message;
}

