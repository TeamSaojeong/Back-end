package com.api.saojeong.kakao.dto;

public record KakaoSearchResponse(
        KakaoMeta meta,
        java.util.List<KakaoPlace> documents
) {}