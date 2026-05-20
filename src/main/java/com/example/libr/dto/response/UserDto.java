package com.example.libr.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private String name;
    private String email;
    private String role;
}
