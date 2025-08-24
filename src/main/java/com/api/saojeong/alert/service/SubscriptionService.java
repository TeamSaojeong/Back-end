package com.api.saojeong.alert.service;

import com.api.saojeong.alert.dto.SubscriptionResponseDto;
import com.api.saojeong.domain.Member;

import java.util.List;

public interface SubscriptionService {
    List<SubscriptionResponseDto> getSub(Member member);
}
