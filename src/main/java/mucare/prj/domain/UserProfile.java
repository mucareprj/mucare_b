package mucare.prj.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfile {
    private Long userId;         // 사용자 ID
    private String nickname;     // 닉네임
    private String name;         // 이름
    private String gender;       // 성별 ('M', 'F', 'Other')
    private LocalDate birthDate; // 생일 (yyyy-MM-dd)
    private LocalTime birthTime; // 태어난 시간 (HH:mm)
    private String bloodType;    // 혈액형 ('A', 'B', 'O', 'AB')
    private String email;
}
