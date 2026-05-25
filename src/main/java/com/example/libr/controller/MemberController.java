package com.example.libr.controller;

import com.example.libr.dto.request.CompleteProfileRequest;
import com.example.libr.dto.request.UpdateMemberRequest;
import com.example.libr.dto.response.MemberResponse;
import com.example.libr.payload.ApiResponse;
import com.example.libr.service.impl.IMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final IMemberService memberService;

    // ── GET /api/v1/members/me
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponse response = memberService.getMyProfile(UUID.fromString(userDetails.getUsername()));
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Profile retrieved successfully")
                .data(response)
                .build());
    }

    // ── POST /api/v1/members/me/complete-profile
    @PostMapping("/me/complete-profile")
    public ResponseEntity<ApiResponse<MemberResponse>> completeProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CompleteProfileRequest request) {
        MemberResponse response = memberService.completeProfile(
                UUID.fromString(userDetails.getUsername()), request);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Profile completed successfully")
                .data(response)
                .build());
    }

    // ── PUT /api/v1/members/me
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateMemberRequest request) {
        MemberResponse response = memberService.updateMyProfile(
                UUID.fromString(userDetails.getUsername()), request);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Profile updated successfully")
                .data(response)
                .build());
    }

    // ── GET /api/v1/members  (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(Pageable pageable) {
        Page<MemberResponse> response = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<MemberResponse>>builder()
                .message("Members retrieved successfully")
                .data(response)
                .build());
    }

    // ── GET /api/v1/members/{id}  (ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable UUID id) {
        MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Member retrieved successfully")
                .data(response)
                .build());
    }

    // ── PUT /api/v1/members/{id}  (ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMemberRequest request) {
        MemberResponse response = memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Member updated successfully")
                .data(response)
                .build());
    }

    // ── POST /api/v1/members/{id}/renew  (ADMIN)
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MemberResponse>> renewMembership(@PathVariable UUID id) {
        MemberResponse response = memberService.renewMembership(id);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .message("Membership renewed successfully")
                .data(response)
                .build());
    }
}
