package com.api.saojeong.alert.dto;

public record SubscriptionResponseDto(
        Long subId,
        Long parkingId,
        String externalId
) {
}
