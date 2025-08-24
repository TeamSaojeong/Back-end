package com.api.saojeong.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PredictionItem(
        String name,
        double lat,
        double lon,
        double dist_km,
        String pred_level
) {}