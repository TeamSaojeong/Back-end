package com.api.saojeong.ai.dto;

public record PredictRequest(
        double lat,
        double lon,
        String arrival,
        double radius,
        int top_k,
        boolean exact_radius,
        boolean list_mode,
        String sort_by,
        boolean fill_external,
        boolean use_places,
        boolean use_weather,
        String google_api_key,  // nullable
        String owm_api_key      // nullable
) {}