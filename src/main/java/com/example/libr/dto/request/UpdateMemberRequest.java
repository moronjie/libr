package com.example.libr.dto.request;

import com.example.libr.entity.Member;
import com.example.libr.enums.MembershipTier;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequest {
    private String phone;
    private String address;
    private String profileImageUrl;

    // Admin only
    private MembershipTier tier;
    private Member.MemberStatus status;
}