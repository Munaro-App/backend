package com.carrot.munaro.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailSignUpRequest(

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 20,
                message = "비밀번호는 8~20자여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)\\S+$",
                message = "비밀번호는 영문과 숫자를 포함하고 공백 없이 입력해야 합니다."
        )
        String password,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 16,
                message = "닉네임은 2~16자여야 합니다.")
        @Pattern(
                regexp = "^[가-힣A-Za-z0-9_]+$",
                message = "닉네임은 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다."
        )
        String nickname

) {
}
