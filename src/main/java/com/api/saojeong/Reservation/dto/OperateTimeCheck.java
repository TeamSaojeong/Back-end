package com.api.saojeong.Reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class OperateTimeCheck {
    private boolean operateCheck; //현재 운영시간인지
    private boolean lastStartCheck; //마지막 예약 가능 시간을 지나는지
    private int remainTime; //남은 예약시간

}
