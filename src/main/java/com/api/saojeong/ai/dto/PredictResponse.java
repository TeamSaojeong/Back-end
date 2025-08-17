package com.api.saojeong.ai.dto;

public record PredictResponse(
        boolean ok,
        java.util.List<PredictionItem> items,
        String logs
) {}