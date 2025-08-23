package com.api.saojeong.SoonOut.exception;

import com.api.saojeong.global.utill.exception.CustomException;


public class SoonOutNotFoundException extends CustomException {
  public SoonOutNotFoundException() {super(SoonOutErrorCode.SOON_OUT_NOT_FOUND);}
}