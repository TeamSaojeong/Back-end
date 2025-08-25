package com.api.saojeong.ai.controller;


import com.api.saojeong.ai.dto.*;
import com.api.saojeong.ai.service.ParkingServiceClient;
import com.api.saojeong.ai.service.PredictionEnricher;
import com.api.saojeong.ai.service.ReverseGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class AiController {
    private final ParkingServiceClient client;


    private final PredictionEnricher enricher; // 위에서 만든 서비스

    @PostMapping("/predict")
    public Mono<PredictResponseAddress> predict(@RequestBody PredictRequest req) {
        return client.predict(req)
                .flatMap(resp ->
                        Mono.zip(
                                enricher.enrich(resp.items()),
                                enricher.enrich(resp.alternatives())

                        ).map(t -> new PredictResponseAddress(
                                resp.ok(),
                                t.getT1(),     // items with address
                                t.getT2()      // alternatives with address
                        ))
                );
    }


    @PostMapping("/train")
    Mono<TrainResponse> train(@RequestBody TrainRequest req) {
        return client.train(req);
    }
}