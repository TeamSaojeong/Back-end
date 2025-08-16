package com.api.saojeong.Report.dto;

import com.api.saojeong.Report.enums.ReportType;

public record CreateReportResponseDto(
        Long memberId,
        Long parkingId,
        Long reportId,
        ReportType reportType
) {
}
