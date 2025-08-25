package com.api.saojeong.ai.dto;

public record AddressedPredictionItem(
        String name,
        double lat,
        double lon,
        double dist_km,
        String pred_level,
        String address       // 지번
            // 도로명
) {}