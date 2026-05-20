package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDto user;
}
