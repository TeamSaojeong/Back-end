package com.api.saojeong.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoMeta(
        Integer total_count,
        Integer pageable_count,
        @JsonProperty("is_end") boolean isEnd, // boolean은 is_ 필드 매핑 주의!
        KakaoSameName sameName
) {}