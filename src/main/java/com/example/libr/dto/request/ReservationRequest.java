package com.example.libr.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "Book ID is required")
    private UUID bookId;
}