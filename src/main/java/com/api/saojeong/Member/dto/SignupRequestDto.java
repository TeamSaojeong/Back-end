package com.api.saojeong.Member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Data
public class SignupRequestDto {
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식으로 작성해주세요.")
    private String memberId;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\S]{8,20}$", message = "영문, 숫자를 포함한 8~20자리 이내로 입력해주세요.")
    private String password;
    private String passwordCheck;

    @NotBlank(message = "이름을 정확하게 입력해주세요.")
    private String name;

//    @Pattern(regexp = "^(19|20)\\d\\d(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])$", message = "올바른 형식으로 작성해주세요.")
//    private String birth;
//
//    @NotBlank(message = "인증번호를 입력해주세요.")
//    private String verifyCode;
}
