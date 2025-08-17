package com.api.saojeong.ai.controller;


import com.api.saojeong.ai.dto.PredictRequest;
import com.api.saojeong.ai.dto.PredictResponse;
import com.api.saojeong.ai.dto.TrainRequest;
import com.api.saojeong.ai.dto.TrainResponse;
import com.api.saojeong.ai.service.ParkingServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parking")
public class AiController {
    private final ParkingServiceClient client;


    @PostMapping("/predict")
    Mono<PredictResponse> predict(@RequestBody PredictRequest req) {
        return client.predict(req);
    }

    @PostMapping("/train")
    Mono<TrainResponse> train(@RequestBody TrainRequest req) {
        return client.train(req);
    }
}