package com.api.saojeong.global.utill.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomApiResponse<T> {

    // status, data, message
    private int status;
    private T data;
    private String message;


    // 성공
    public static <T> CustomApiResponse<T> createSuccess(int status, T data, String message) {
        return new CustomApiResponse<>(status, data, message);
    }
    public static <T> CustomApiResponse<T> createSuccessLogin(int status, T data, String message, String Token) {
        return new CustomApiResponse<>(status, data, message);
    }
    public static <T> CustomApiResponse<T> createSuccessWithoutData(int status, String message) {
        return new CustomApiResponse<>(status, null, message);
    }


    // 실패
    public static <T> CustomApiResponse<T> createFailWithoutData(int status, String message) {
        return new CustomApiResponse<>(status, null, message);
    }
    public static <T> CustomApiResponse<T> createFailWithout(int status,String message){
        return new CustomApiResponse<>(status, null, message);
    }



}