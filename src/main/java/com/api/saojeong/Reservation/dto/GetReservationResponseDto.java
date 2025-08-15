package com.api.saojeong.Reservation.dto;

import com.api.saojeong.Reservation.enums.ButtonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetReservationResponseDto{
        private ButtonStatus buttonStaus;
        private int soonOutTime;
        private int remainReservationTime;
}
