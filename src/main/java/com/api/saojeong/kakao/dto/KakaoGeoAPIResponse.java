package com.api.saojeong.kakao.dto;

import lombok.Data;

import java.util.List;

//지오코딩 변환 응답 구성
@Data
public class KakaoGeoAPIResponse {
    private List<Document> documents;

    @Data
    public static class Document{
        private Address address;

        @Data
        public static class Address{
            private String x; //경도
            private String y; //위도
        }
    }
}

//{
//    "documents": [
//        {
//            "address": {
//                "x": "126.97",
//                "y": "37.56"
//            }
//        }
//    ]
//}
