package com.api.saojeong.Parking.service;

import com.api.saojeong.Member.repository.MemberRepository;
import com.api.saojeong.Parking.dto.CreateParkingRequestDto;
import com.api.saojeong.Parking.dto.CreateParkingResponseDto;
import com.api.saojeong.Parking.dto.GetMemberParkingResponseDto;
import com.api.saojeong.Parking.dto.ParkingTimeDto;
import com.api.saojeong.Parking.enums.ParkingKind;
import com.api.saojeong.Parking.exception.S3UploadFailedException;
import com.api.saojeong.Parking.repository.ParkingRepository;
import com.api.saojeong.domain.Member;
import com.api.saojeong.domain.Parking;
import com.api.saojeong.domain.ParkingTime;
import com.api.saojeong.global.s3.S3Service;
import com.api.saojeong.global.security.LoginMember;
import com.api.saojeong.kakao.dto.GeoResponse;
import com.api.saojeong.kakao.service.ParkingGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class memberParkingServiceImple implements memberParkingService {

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
            //에러수정하기
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

        for (ParkingTimeDto t : req.getAvailableTimes()) {
            ParkingTime time = ParkingTime.builder()
                    .start(LocalTime.parse(t.getStart())) // "HH:mm"
                    .end(LocalTime.parse(t.getEnd()))
                    .build();

            parking.addParkingTime(time);
        }

        Parking res = parkingRepository.save(parking);

        // 엔티티 → DTO 변환
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
}
