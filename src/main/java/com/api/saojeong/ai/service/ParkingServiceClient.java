package com.api.saojeong.ai.service;


import com.api.saojeong.ai.dto.PredictRequest;
import com.api.saojeong.ai.dto.PredictResponse;
import com.api.saojeong.ai.dto.TrainRequest;
import com.api.saojeong.ai.dto.TrainResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ParkingServiceClient {
    private final WebClient client;
    ParkingServiceClient(WebClient parkingClient) { this.client = parkingClient; }

    public Mono<PredictResponse> predict(PredictRequest req) {
        return client.post()
                .uri("/predict")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(PredictResponse.class);
    }

    public Mono<TrainResponse> train(TrainRequest req) {
        return client.post()
                .uri("/train")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(TrainResponse.class);
    }}