package com.api.saojeong.Reservation.exception;

import com.api.saojeong.global.utill.exception.CustomException;

public class NoOperateTime extends CustomException {
  public NoOperateTime() {
    super(ReservationErrorCode.NO_OPERATE_TIME);
  }
}
