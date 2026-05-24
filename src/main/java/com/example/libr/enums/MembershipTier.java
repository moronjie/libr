package com.example.libr.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipTier {

    BASIC(2, 0),
    STUDENT(5, 2),
    PREMIUM(10, 3);

    private final int maxBorrowLimit;
    private final int fineGraceDays;
}

