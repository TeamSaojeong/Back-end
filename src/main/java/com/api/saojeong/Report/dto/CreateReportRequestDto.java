package com.api.saojeong.Report.dto;


import com.api.saojeong.Report.enums.ReportType;
import lombok.Getter;

@Getter
public class CreateReportRequestDto {

    private String carNum;

    private ReportType type;

}
