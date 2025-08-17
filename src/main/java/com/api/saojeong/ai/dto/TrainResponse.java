package com.api.saojeong.ai.dto;


public record TrainResponse(
        boolean ok,
        String stdout
) {}