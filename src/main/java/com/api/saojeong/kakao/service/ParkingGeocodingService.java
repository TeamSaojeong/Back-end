package com.api.saojeong.kakao.service;

import com.api.saojeong.kakao.dto.GeoResponse;
import com.api.saojeong.kakao.dto.KakaoGeoAPIResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.util.List;

@Service
public class ParkingGeocodingService {

    @Value("${kakao.rest-api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    //주소-> 위도, 경도 반환
    public GeoResponse getGeodoing(String address){


        //요청 url
        String url = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query",address)
                .build()
                .toUriString();

        //헤더값
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);

        //헤더만 포함된 HTTP요청 객체
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("Kakao API Request URL: " + url);

        //(exchange메서드)요청을 보내고 응답받기
        ResponseEntity<KakaoGeoAPIResponse> response = restTemplate.exchange(
                url,  //api주소
                HttpMethod.GET,
                entity, //헤더
                KakaoGeoAPIResponse.class //응답 형태
        );

        String rawResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        System.out.println("Kakao API Raw JSON: " + rawResponse);

        //리스트에서 주소가 있는 documents 꺼냄
        List<KakaoGeoAPIResponse.Document> documents = response.getBody().getDocuments();
        if (documents.isEmpty()) {
            throw new IllegalArgumentException("주소로 좌표를 찾을 수 없습니다.");
        }

        KakaoGeoAPIResponse.Document.Address addr = documents.get(0).getAddress();
        double lat = Double.parseDouble(addr.getY());
        double lng = Double.parseDouble(addr.getX());

        return new GeoResponse(lat, lng);

    }
}
