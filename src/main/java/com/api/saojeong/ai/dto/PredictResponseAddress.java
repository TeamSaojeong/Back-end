package com.api.saojeong.ai.dto;
public record PredictResponseAddress(
        boolean ok,
        java.util.List<AddressedPredictionItem> items,
        java.util.List<AddressedPredictionItem> alternatives
) {}