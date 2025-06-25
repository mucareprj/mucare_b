package mucare.prj.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class User {

    private Long id; // PK
    private String email; // E-mail UNIQUE
    private String password; // PW  

    private String provider; // 소셜 로그인 종류
    private String providerId; // 소셜 로그인 ID

    private LocalDateTime createdAt; // 회원가입 일자
    private LocalDateTime lastLogin; // 마지막 로그인 일자

    private Boolean isActive; // 활성 / 비활성 상태

}
