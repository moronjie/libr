package com.example.libr.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;
}

