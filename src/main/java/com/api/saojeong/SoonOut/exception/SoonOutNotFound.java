package com.api.saojeong.SoonOut.exception;

import com.api.saojeong.global.utill.exception.CustomException;


public class SoonOutNotFound extends CustomException {
  public SoonOutNotFound() {super(SoonOutErrorCode.SOON_OUT_NOT_FOUND);}
}