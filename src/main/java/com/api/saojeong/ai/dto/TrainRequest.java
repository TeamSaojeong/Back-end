package com.api.saojeong.ai.dto;

public record TrainRequest(
        String data_glob,
        String model_dir,
        boolean no_gpu
) {}
