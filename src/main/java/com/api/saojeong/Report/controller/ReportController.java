package com.api.saojeong.Report.controller;

import com.api.saojeong.Report.dto.CreateReportRequestDto;
import com.api.saojeong.Report.dto.CreateReportResponseDto;
import com.api.saojeong.Report.service.ReportService;
import com.api.saojeong.domain.Member;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.global.utill.response.CustomApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/parking/{parkingId}/report")
    public ResponseEntity<CustomApiResponse<?>> createReport(@LoginMember Member member,
                                                             @PathVariable Long parkingId,
                                                             @Valid @RequestPart("request") CreateReportRequestDto req,
                                                             @RequestPart(required = false) MultipartFile image){
        CreateReportResponseDto res = reportService.createReport(member, parkingId, req,image);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CustomApiResponse.createSuccess(
                        HttpStatus.OK.value(),
                        res,
                        "신고 접수 완료"
                ));
    }
}
