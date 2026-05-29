package com.example.libr.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MembershipTier {

    BASIC(2, 0, 14),
    STUDENT(5, 0, 21),
    PREMIUM(10, 3, 30);

    private final int maxBorrowLimit;
    private final int fineGraceDays;
    private final int loanPeriodDays;
}
