package com.api.saojeong.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

    private final WebClient kakaoWebClient;

    public Mono<AddressPair> reverse(double lat, double lon) {
        // Kakao: /v2/local/geo/coord2address.json?x={lon}&y={lat}
        return kakaoWebClient.get()
                .uri(uri -> uri
                        .path("/v2/local/geo/coord2address.json")
                        .queryParam("x", lon)
                        .queryParam("y", lat)
                        .build())
                .retrieve()
                .bodyToMono(KakaoCoord2AddressResponse.class)
                .map(resp -> {
                    var doc = (resp.documents() != null && !resp.documents().isEmpty())
                            ? resp.documents().get(0) : null;
                    String addr = doc != null && doc.address() != null ? doc.address().address_name() : null;
                    String road = doc != null && doc.road_address() != null ? doc.road_address().address_name() : null;
                    return new AddressPair(addr, road);
                })
                .onErrorReturn(new AddressPair(null, null)); // 실패해도 전체 흐름은 유지
    }

    // ===== DTOs for Kakao response =====
    public record AddressPair(String address, String roadAddress) {}
    public record KakaoCoord2AddressResponse(java.util.List<Document> documents) {}
    public record Document(Address address, RoadAddress road_address) {}
    public record Address(String address_name) {}
    public record RoadAddress(String address_name) {}
}
