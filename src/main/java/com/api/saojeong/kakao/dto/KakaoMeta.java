package com.api.saojeong.kakao.dto;

public record KakaoMeta(
        Integer total_count,
        Integer pageable_count,
        Boolean is_end
) {}