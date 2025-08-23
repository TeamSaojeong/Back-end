package com.api.saojeong.alert.controller;

import com.api.saojeong.alert.repository.UserAlertRepository;
import com.api.saojeong.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertFeedController {
    private final UserAlertRepository repo;

    @GetMapping("/feed")
    public List<AlertItemDto> feed(@AuthenticationPrincipal Member me,
                                   @RequestParam(required = false) OffsetDateTime since) {
        return repo.findNewAlerts(me.getId(), since).stream()
                .map(ua -> new AlertItemDto(
                        ua.getId(), ua.getType(), ua.getSoonoutId(),
                        ua.getTitle(), ua.getBody(), ua.getDeeplink(),
                        ua.getCreatedAt(), ua.getPayloadJson()))
                .toList();
    }

    @PostMapping("/consume")
    public void consume(@AuthenticationPrincipal Member me, @RequestBody Ids req) {
        repo.consumeAlerts(me.getId(), req.ids(), OffsetDateTime.now());
    }

    public record AlertItemDto(Long id, String type, Long soId, String title, String body,
                               String deeplink, OffsetDateTime createdAt, String payloadJson) {}
    public record Ids(List<Long> ids) {}
}