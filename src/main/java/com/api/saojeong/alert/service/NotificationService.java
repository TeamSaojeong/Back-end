package com.api.saojeong.alert.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@saojeong.com}")
    private String from;

    public void sendSoonOutEmail(String to, String placeName, double lat, double lng, int minute) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("[사오정] 곧 나감 알림");
            msg.setText(String.format("'%s'에서 %d분 내로 자리가 날 예정입니다.\n위치: %.6f, %.6f",
                    placeName, minute, lat, lng));
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }}