package com.api.saojeong.alert.controller;

import com.api.saojeong.alert.repository.NotificationEventRepository;
import com.api.saojeong.alert.repository.UserAlertRepository;
import com.api.saojeong.alert.service.NotificationService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertFeedController {
    private final UserAlertRepository repo;

    @GetMapping("/feed")
    public List<AlertItemDto> feed(@LoginMember Member me) {
        return repo.findById(me.getId()).stream()
                .map(ua -> new AlertItemDto(
                        ua.getId(), ua.getType(), ua.getSoonoutId(),
                        ua.getTitle(), ua.getBody(), ua.getDeeplink(),
                        ua.getCreatedAt()))
                .toList();
    }

    @PostMapping("/consume")
    public void consume(@AuthenticationPrincipal Member me, @RequestBody Ids req) {
        repo.consumeAlerts(me.getId(), req.ids(), OffsetDateTime.now());
    }

    public record AlertItemDto(Long id, String type, Long soId, String title, String body,
                               String deeplink, OffsetDateTime createdAt) {}
    public record Ids(List<Long> ids) {}
}