package com.api.saojeong.Report.service;

import com.api.saojeong.Parking.exception.ParkingNotFoundException;
import com.api.saojeong.Parking.exception.S3UploadFailedException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.Report.dto.CreateReportRequestDto;
import com.api.saojeong.Report.dto.CreateReportResponseDto;
import com.api.saojeong.Report.enums.ReportType;
import com.api.saojeong.Report.exception.MissingCarNumException;
import com.api.saojeong.Report.exception.MissingImageException;
import com.api.saojeong.Report.repository.ReportRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.Report;
import com.api.saojeong.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final S3Service s3Service;
    private final ParkingRepository parkingRepository;
    private final ReportRepository reportRepository;

    @Override
    public CreateReportResponseDto createReport(Member member, Long parkingId, CreateReportRequestDto req, MultipartFile image) {
        //장소 확인
        Parking parking = parkingRepository.findByIdAndOperate(parkingId, true)
                .orElseThrow(ParkingNotFoundException::new);

        String url ="";
        //예약 시간 초과 점유 및 무단 주차 차량
        if(req.getType() == ReportType.OVERTIME_OR_ILLEGAL){
            if(req.getCarNum().isEmpty())
                throw new MissingCarNumException();
            if(image.isEmpty())
                throw new MissingImageException();

            //사진 s3저장
            try {
                url = s3Service.uploadFile(image,"report");
            } catch (IOException e) {
                throw new S3UploadFailedException();
            }
        }

        Report report = Report.builder()
                .parking(parking)
                .member(member)
                .carNum(req.getCarNum())
                .type(req.getType())
                .image(url)
                .build();

        Report res = reportRepository.save(report);

        return new CreateReportResponseDto(res.getMember().getId(), res.getParking().getId(),
                res.getId(), res.getType());
    }
}
