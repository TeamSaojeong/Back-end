package com.api.saojeong.Report.service;

import com.api.saojeong.Report.dto.CreateReportRequestDto;
import com.api.saojeong.Report.dto.CreateReportResponseDto;
import com.api.saojeong.domain.Member;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface ReportService {
    CreateReportResponseDto createReport(Member member, Long parkingId, @Valid CreateReportRequestDto req, MultipartFile image);
}
