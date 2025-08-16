package com.api.saojeong.memberLocation.controller;



import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.MemberLocation;
import com.api.saojeong.domain.MemberRole;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import com.api.saojeong.memberLocation.repository.MemberLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;

    @RestController
    @RequestMapping("/api/members/me")
    @RequiredArgsConstructor
    public class MemberPresenceController {

        private final MemberRepository memberRepo;
        private final MemberLocationRepository locationRepo;

        @PostMapping("/location")
        public ResponseEntity<?> updateLocation(@LoginMember Member memberId,
                                                @RequestParam double lat,
                                                @RequestParam double lng) {
            Member m = memberRepo.findById(memberId.getId()).orElseThrow();
            var now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
            var loc = locationRepo.findByMemberId(memberId.getId()).orElse(null);
            if (loc == null) {
                loc = MemberLocation.builder().member(m).lat(lat).lng(lng).updatedAt(now).build();
            } else {
                loc.setLat(lat); loc.setLng(lng); loc.setUpdatedAt(now);
            }
            locationRepo.save(loc);
            return ResponseEntity.ok(CustomApiResponse.createSuccessWithoutData(200, "위치 업데이트 완료"));
        }
    }
