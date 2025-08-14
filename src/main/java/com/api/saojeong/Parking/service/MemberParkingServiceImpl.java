package com.api.saojeong.Parking.service;

import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.Parking.dto.*;
import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.Parking.exception.ParkingNotFoundException;
import com.api.saojeong.Parking.exception.S3UploadFailedException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.ParkingTime;
import com.api.saojeong.global.s3.S3Service;
import com.api.saojeong.kakao.dto.GeoResponse;
import com.api.saojeong.kakao.service.ParkingGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberParkingServiceImpl implements MemberParkingService {

    private final MemberRepository memberRepository;
    private final ParkingRepository parkingRepository;
    private final ParkingGeocodingService parkingGeocodingService;
    private final S3Service s3Service;

    //개인 주차장 추가
    @Override
    public CreateParkingResponseDto save(Member member, MultipartFile image, CreateParkingRequestDto req) {
        //멤버 확인
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(); //예외 추가하기

        //좌표 변환
        GeoResponse geo = parkingGeocodingService.getGeodoing(req.getAddress());
        double lat = geo.getLat();
        double lng = geo.getLng();

        //사진 s3저장
        String url;
        try {
            url = s3Service.uploadFile(image);
        } catch (IOException e) {
            throw new S3UploadFailedException();
        }

        Parking parking = Parking.builder()
                .member(member)
                .name(req.getName())
                .address(req.getAddress())
                .pLat(lat)
                .pLng(lng)
                .kind(ParkingKind.PERSONAL)
                .charge(req.getCharge())
                .photo(url)
                .operate(true)
                .content(req.getContent())
                .build();

        for (ParkingTimeDto t : req.getOperateTimes()) {
            ParkingTime time = ParkingTime.builder()
                    .start(LocalTime.parse(t.getStart())) // "HH:mm"
                    .end(LocalTime.parse(t.getEnd()))
                    .build();

            parking.addParkingTime(time);
        }

        Parking res = parkingRepository.save(parking);

        List<ParkingTimeDto> timeDtos = res.getParkingTimes().stream()
                .map(t -> new ParkingTimeDto(
                        t.getStart().toString(),
                        t.getEnd().toString()
                ))
                .toList();


        return new CreateParkingResponseDto(res.getId(), res.getName(), timeDtos, res.getCharge());
    }

    //개인 주차장 관리 화면 조회
    @Override
    public List<GetMemberParkingResponseDto> getMemberParking(Member member) {
        List<Parking> memberParkings = parkingRepository.findByMemberIdAndKind(member.getId(), ParkingKind.PERSONAL);

        List<GetMemberParkingResponseDto> res = new ArrayList<>();

        for(Parking p : memberParkings){
            res.add(new  GetMemberParkingResponseDto(
                    p.getId(),
                    p.getName(),
                    p.isOperate()
            ));
        }

        return res;
    }

    //토글로 주차장 활성화 수정
    @Transactional
    @Override
    public ModifyMemberParkingOperResponseDto modifyMemberParkingOper(Member member, Long parkingId) {
        //주차장이 없을 경우
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(ParkingNotFoundException::new);

        //전환
        parking.setOperate(!parking.isOperate());

        return new ModifyMemberParkingOperResponseDto(parking.getId(), parking.isOperate());
    }

    //개인 주차장 상세 조회
    @Override
    public GetDetailMemberParkingResponseDto getDetailMemberParking(Member member, Long parkingId) {
        //주차장이 없을 경우
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(ParkingNotFoundException::new);

        List<ParkingTimeDto> timeDtos = parking.getParkingTimes().stream()
                .map(t -> new ParkingTimeDto(
                        t.getStart().toString(),
                        t.getEnd().toString()
                ))
                .toList();

        GetDetailMemberParkingResponseDto ResDto = GetDetailMemberParkingResponseDto.builder()
                .parkingName(parking.getName())
                .address(parking.getAddress())
                .photo(parking.getPhoto())
                .content(parking.getContent())
                .operateTimes(timeDtos)
                .charge(parking.getCharge())
                .build();

        return ResDto;
    }

    //개인 주차장 수정
    @Override
    public CreateParkingResponseDto updateMemberParking(Member member, Long parkingId, UpdateMemberParkingRequestDto request, MultipartFile image) {

        //주차장이 없을 경우
        Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(ParkingNotFoundException::new);

        // 텍스트 수정 (null 체크로 기존 값 유지)
        if (request.getName() != null)
            parking.setName(request.getName() );

        if (request.getAddress() != null) {
            parking.setAddress(request.getAddress());

            //좌표 변환
            GeoResponse geo = parkingGeocodingService.getGeodoing(request.getAddress());
            parking.setPLat(geo.getLat());
            parking.setPLng(geo.getLng());
        }

        if (request.getContent() != null)
            parking.setContent(request.getContent());

        if (request.getCharge() != null)
            parking.setCharge(request.getCharge());

        //사용 가능 시간이 수정 된다면
        if(request.getOperateTimes() != null){
            parking.getParkingTimes().clear();

            for (ParkingTimeDto t : request.getOperateTimes()) {
                ParkingTime time = ParkingTime.builder()
                        .start(LocalTime.parse(t.getStart())) // "HH:mm"
                        .end(LocalTime.parse(t.getEnd()))
                        .build();

                parking.addParkingTime(time);
            }
        }

        //사진이 수정된다면
        if(image != null){

            String key = parking.getPhoto().replace("https://parkherebucket.s3.ap-northeast-2.amazonaws.com/", "");
            System.out.println(key);
            s3Service.deleteFile(key);

            //사진 s3저장
            try {
                String url = s3Service.uploadFile(image);
                parking.setPhoto(url);
            } catch (IOException e) {
                throw new S3UploadFailedException();
            }

        }

        List<ParkingTimeDto> timeDtos = parking.getParkingTimes().stream()
                .map(t -> new ParkingTimeDto(
                        t.getStart().toString(),
                        t.getEnd().toString()
                ))
                .toList();

        return new CreateParkingResponseDto(parking.getId(), parking.getName(), timeDtos, parking.getCharge());
    }
}
