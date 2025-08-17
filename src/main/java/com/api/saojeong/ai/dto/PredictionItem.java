package com.api.saojeong.ai.dto;

public record PredictionItem(
        String prkCd,
        String prkNm,
        String areaCd,
        double dist_km,
        String pred_level,
        double score,
        double p_여유,
        double p_보통,
        double p_혼잡
) {}