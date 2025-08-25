// CoordinateRefiner.java
package com.api.saojeong.ai.service;

import reactor.core.publisher.Mono;

public interface CoordinateRefiner {
    // 기존: 기본 반경으로 검색
    default Mono<LatLng> refine(String name, double lat, double lon) {
        return refine(name, lat, lon, 1500); // 기본 1.5km
    }

    // 새로 추가: 호출자가 반경을 넘길 수 있게
    Mono<LatLng> refine(String name, double lat, double lon, int searchRadiusM);

    record LatLng(double lat, double lon, String source) {}
}
