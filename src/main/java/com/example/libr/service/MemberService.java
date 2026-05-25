package com.example.libr.service;

import com.example.libr.dto.request.CompleteProfileRequest;
import com.example.libr.dto.request.UpdateMemberRequest;
import com.example.libr.dto.response.MemberResponse;
import com.example.libr.entity.Member;
import com.example.libr.entity.User;
import com.example.libr.enums.MembershipTier;
import com.example.libr.exception.AppException;
import com.example.libr.repository.MemberRepository;
import com.example.libr.repository.UserRepository;
import com.example.libr.service.impl.IMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// modules/member/MemberService.java
@Service
@RequiredArgsConstructor
public class MemberService implements IMemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_MEMBERSHIP_DURATION_DAYS = 365;

    //Called automatically by AuthService after email verification

    public void initializeMember(User user) {
        if (memberRepository.existsByUser(user)) return;

        Member member = Member.builder()
                .user(user)
                .tier(MembershipTier.BASIC)
                .status(Member.MemberStatus.ACTIVE)
                .profileComplete(false)
                .membershipExpiresAt(LocalDate.now().plusDays(DEFAULT_MEMBERSHIP_DURATION_DAYS))
                .build();

        memberRepository.save(member);
    }

    //Complete Profile (first time after registration)

    public MemberResponse completeProfile(UUID userId, CompleteProfileRequest request) {
        Member member = findMemberByUserId(userId);

        if (member.isProfileComplete()) {
            throw new AppException("Profile already completed", HttpStatus.BAD_REQUEST);
        }

        if (memberRepository.existsByNationalId(request.getNationalId())) {
            throw new AppException("National ID already in use", HttpStatus.CONFLICT);
        }

        member.setPhone(request.getPhone());
        member.setAddress(request.getAddress());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setNationalId(request.getNationalId());
        member.setProfileImageUrl(request.getProfileImageUrl());
        member.setProfileComplete(true);

        return mapToResponse(memberRepository.save(member));
    }

    //Get My Profile

    public MemberResponse getMyProfile(UUID userId) {
        return mapToResponse(findMemberByUserId(userId));
    }

    //Get Member by ID (Admin)

    public MemberResponse getMemberById(UUID memberId) {
        return mapToResponse(findMember(memberId));
    }

    //Get All Members (Admin)

    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(this::mapToResponse);
    }

    //Update My Profile

    public MemberResponse updateMyProfile(UUID userId, UpdateMemberRequest request) {
        Member member = findMemberByUserId(userId);
        applyBasicUpdates(member, request);
        return mapToResponse(memberRepository.save(member));
    }

    //Update Any Member (Admin)

    public MemberResponse updateMember(UUID memberId, UpdateMemberRequest request) {
        Member member = findMember(memberId);
        applyBasicUpdates(member, request);

        if (request.getTier() != null) member.setTier(request.getTier());
        if (request.getStatus() != null) member.setStatus(request.getStatus());

        return mapToResponse(memberRepository.save(member));
    }

    //Renew Membership (Admin)

    public MemberResponse renewMembership(UUID memberId) {
        Member member = findMember(memberId);

        LocalDate base = member.getMembershipExpiresAt().isBefore(LocalDate.now())
                ? LocalDate.now()
                : member.getMembershipExpiresAt();

        member.setMembershipExpiresAt(base.plusDays(DEFAULT_MEMBERSHIP_DURATION_DAYS));
        member.setStatus(Member.MemberStatus.ACTIVE);

        return mapToResponse(memberRepository.save(member));
    }

    //Expire Memberships — runs every midnight

    @Scheduled(cron = "0 0 0 * * *")
    public void expireOutdatedMemberships() {
        List<Member> expired = memberRepository.findExpiredActiveMembers(LocalDate.now());
        expired.forEach(m -> m.setStatus(Member.MemberStatus.EXPIRED));
        memberRepository.saveAll(expired);
    }

    //Validate Can Borrow (called by Borrowing module)

    public void validateCanBorrow(Member member, int currentBorrowCount) {
        if (!member.isProfileComplete()) {
            throw new AppException(
                    "Please complete your profile before borrowing",
                    HttpStatus.FORBIDDEN);
        }

        if (member.getStatus() == Member.MemberStatus.SUSPENDED) {
            throw new AppException(
                    "Your account is suspended. Contact the library for assistance",
                    HttpStatus.FORBIDDEN);
        }

        if (member.getStatus() == Member.MemberStatus.EXPIRED) {
            throw new AppException(
                    "Your membership has expired. Please renew to borrow books",
                    HttpStatus.FORBIDDEN);
        }

        if (currentBorrowCount >= member.getTier().getMaxBorrowLimit()) {
            throw new AppException(
                    "You have reached your borrow limit of "
                            + member.getTier().getMaxBorrowLimit(),
                    HttpStatus.FORBIDDEN);
        }
    }

    private void applyBasicUpdates(Member member, UpdateMemberRequest request) {
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getAddress() != null) member.setAddress(request.getAddress());
        if (request.getProfileImageUrl() != null)
            member.setProfileImageUrl(request.getProfileImageUrl());
    }

    public Member findMemberByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return memberRepository.findByUser(user)
                .orElseThrow(() -> new AppException("Member profile not found",
                        HttpStatus.NOT_FOUND));
    }

    public Member findMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException("Member not found", HttpStatus.NOT_FOUND));
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .dateOfBirth(member.getDateOfBirth())
                .nationalId(member.getNationalId())
                .profileImageUrl(member.getProfileImageUrl())
                .tier(member.getTier().name())
                .maxBorrowLimit(member.getTier().getMaxBorrowLimit())
                .fineGraceDays(member.getTier().getFineGraceDays())
                .status(member.getStatus().name())
                .profileComplete(member.isProfileComplete())
                .membershipExpiresAt(member.getMembershipExpiresAt())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
