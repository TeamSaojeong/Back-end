package com.api.saojeong.ai.service;

import com.api.saojeong.ai.dto.AddressedPredictionItem;

import com.api.saojeong.ai.dto.PredictionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionEnricher {

    private final ReverseGeocodingService reverse;

    // (선택) 단순 메모리 캐시: 같은 좌표 반복 요청 시 API 호출 절감
    private final java.util.Map<String, ReverseGeocodingService.AddressPair> cache = new java.util.concurrent.ConcurrentHashMap<>();

    public Mono<List<AddressedPredictionItem>> enrich(List<PredictionItem> items) {
        if (items == null || items.isEmpty()) return Mono.just(List.of());
        items.stream().limit(5)
                .forEach(i -> System.out.printf("[ENRICH IN] %s lat=%.8f lon=%.8f%n", i.name(), i.lat(), i.lon()));


        return Flux.fromIterable(items)
                .flatMap(item -> {
                    String key = roundKey(item.lat(), item.lon()); // 소수점 라운딩 키
                    Mono<ReverseGeocodingService.AddressPair> addrMono =
                            Mono.defer(() -> {
                                        var cached = cache.get(key);
                                        if (cached != null) return Mono.just(cached);
                                        return reverse.reverse(item.lat(), item.lon())
                                                .doOnNext(p -> cache.put(key, p));
                                    })
                                    .timeout(Duration.ofSeconds(2))         // 개별 역지오 타임아웃
                                    .onErrorReturn(new ReverseGeocodingService.AddressPair(null, null));

                    return addrMono.map(p -> new AddressedPredictionItem(
                            item.name(),
                            item.lat(),
                            item.lon(),
                            item.dist_km(),
                            item.pred_level(),
                            p.address()

                    ));
                }, /*concurrency*/ 8)                                  // 동시 역지오 개수 제한
                .collectList();
    }

    private String roundKey(double lat, double lon) {
        // 1e-4(약 11m) 수준 라운딩로 캐시 히트율↑ (원하는 정밀도로 조정)
        double rl = Math.round(lat * 1e4) / 1e4;
        double ro = Math.round(lon * 1e4) / 1e4;
        return rl + "," + ro;
    }
}
