package com.example.libr.service.impl;

import com.example.libr.dto.request.CompleteProfileRequest;
import com.example.libr.dto.request.UpdateMemberRequest;
import com.example.libr.dto.response.MemberResponse;
import com.example.libr.entity.Member;
import com.example.libr.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IMemberService {

    void initializeMember(User user);

    MemberResponse completeProfile(UUID userId, CompleteProfileRequest request);

    MemberResponse getMyProfile(UUID userId);

    MemberResponse getMemberById(UUID memberId);

    Page<MemberResponse> getAllMembers(Pageable pageable);

    MemberResponse updateMyProfile(UUID userId, UpdateMemberRequest request);

    MemberResponse updateMember(UUID memberId, UpdateMemberRequest request);

    MemberResponse renewMembership(UUID memberId);

    void validateCanBorrow(Member member, int currentBorrowCount);

    Member findMemberByUserId(UUID userId);

    Member findMember(UUID memberId);
}
