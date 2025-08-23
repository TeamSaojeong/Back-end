package com.api.saojeong.alert.service;


import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;


    @Value("${app.mail.from:no-reply@saojeong.com}")
    private String from;

    @Value("${app.mail.fromName:ParkHere}")
    private String fromName; //발신 표시 이름

    public void sendSoonOutEmail(String to, String placeName, int minute,String address) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, false, "UTF-8");
            h.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            h.setTo(to);
            h.setSubject("[PARKHERE] 곧 나감 알림");
            h.setText(String.format("%s에서 %d분 내로 자리가 날 예정입니다.\n주소: %s",
                    placeName, minute, address));
            mailSender.send(msg);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
    public void sendReservationEndingEmail(String to, String placeName, int minute) {
        try {

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, false, "UTF-8");
            h.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            h.setTo(to);
            h.setSubject("[PARKHERE] 사용시간 종료 임박 알림");

            String body;
            if(placeName.equals("사용중인 주차장")){
                body = String.format(
                        """
                        %s에서 %d분 후 예상 이용 시간이 종료됩니다.
                        
                        아직 출차 계획이 없으시다면
                        서비스 페이지에서 ‘연장하기’를 눌러 주세요!
                        
                        출차 예정이신가요?
                        서비스 페이지에서 ‘곧 나감’을 누르시면 포인트가 즉시 적립되고,
                        근처 운전자에게 알림이 전달됩니다.
                        한 번의 터치로 다음 이용자를 도와주세요!
                        """
                        , placeName, minute);
            }
            else{
                body = String.format(
                        """
                        %s에서 %d분 후 이용이 종료됩니다.
                        
                        출차 예정이신가요?
                        서비스 페이지에서 ‘곧 나감’을 누르시면 포인트가 즉시 적립되고,
                        근처 운전자에게 알림이 전달됩니다.
                        한 번의 터치로 다음 이용자를 도와주세요!
                        """
                        , placeName, minute);
            }
            h.setText(body, false);

            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}