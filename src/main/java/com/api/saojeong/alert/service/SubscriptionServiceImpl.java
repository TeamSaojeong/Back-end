package com.api.saojeong.alert.service;

import com.api.saojeong.alert.dto.SubscriptionResponseDto;
import com.api.saojeong.alert.repository.AlertSubscriptionRepository;
import com.api.saojeong.domain.AlertSubscription;
import com.api.saojeong.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService{

    private final AlertSubscriptionRepository subRepo;

    @Override
    public List<SubscriptionResponseDto> getSub(Member member) {

        List<AlertSubscription> subs = subRepo.findByMemberIdAndActive(member.getId(), true);

        List<SubscriptionResponseDto> res = new ArrayList<>(subs.size());
        for(AlertSubscription sub : subs){

            //true = 개인주차장
            boolean personal = (sub.getParking() != null);
            Long parkingId = personal ? sub.getParking().getId() : null;
            String externalId = personal ? null : sub.getExternalId();

            res.add(new SubscriptionResponseDto(
                    sub.getId(),
                    parkingId,
                    externalId
            ));
        }

        return res;
    }
}
