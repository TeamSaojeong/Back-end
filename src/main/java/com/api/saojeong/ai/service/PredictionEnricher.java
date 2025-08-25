// src/main/java/com/api/saojeong/ai/service/PredictionEnricher.java
package com.api.saojeong.ai.service;

import com.api.saojeong.ai.dto.AddressedPredictionItem;
import com.api.saojeong.ai.dto.PredictionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionEnricher {

    private final ReverseGeocodingService reverse;
    private final CoordinateRefiner refiner;

    private final ConcurrentHashMap<String, ReverseGeocodingService.AddressPair> addrCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CoordinateRefiner.LatLng> coordCache = new ConcurrentHashMap<>();

    public Mono<List<AddressedPredictionItem>> enrich(List<PredictionItem> items) {
        if (items == null || items.isEmpty()) return Mono.just(List.of());

        return Flux.fromIterable(items)
                .flatMap(item -> {
                    String coarseKey = String.format("%.2f,%.2f", item.lat(), item.lon());
                    String nameKey   = item.name() == null ? "" : item.name().trim();
                    String k         = nameKey + "|" + coarseKey;

                    Mono<CoordinateRefiner.LatLng> refined =
                            Mono.defer(() -> {
                                        var c = coordCache.get(k);
                                        if (c != null) return Mono.just(c);
                                        return refiner.refine(item.name(), item.lat(), item.lon())
                                                .doOnNext(v -> {
                                                    coordCache.put(k, v);
                                                    log.debug("[coord] {} -> ({}, {}) via {}", item.name(), v.lat(), v.lon(), v.source());
                                                });
                                    })
                                    .timeout(Duration.ofMillis(1200)) // 4.5s -> 1.2s
                                    .onErrorReturn(new CoordinateRefiner.LatLng(item.lat(), item.lon(), "original-timeout"));

                    return refined.flatMap(coord -> {
                        double lat = coord.lat();
                        double lon = coord.lon();
                        String addrKey = String.format("%.6f,%.6f", lat, lon);

                        Mono<ReverseGeocodingService.AddressPair> addr =
                                Mono.defer(() -> {
                                            var cached = addrCache.get(addrKey);
                                            if (cached != null) return Mono.just(cached);
                                            return reverse.reverse(lat, lon)
                                                    .doOnNext(p -> {
                                                        addrCache.put(addrKey, p);
                                                        log.debug("[addr] ({},{}) -> {}", lat, lon, p.address());
                                                    });
                                        })
                                        .timeout(Duration.ofMillis(1200)) // 2s -> 1.2s
                                        .onErrorReturn(new ReverseGeocodingService.AddressPair(null, null));

                        return addr.map(p -> new AddressedPredictionItem(
                                item.name(), lat, lon, item.dist_km(), item.pred_level(),
                                p.address()
                        ));
                    });
                }, 12) // 동시성 8 -> 12 (네트워크 호출 병렬)
                .collectList();
    }
}
