package mucare.prj.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProfileDTO {
    
    private Long userId;
    private String nickname;
    private String name;
    private String gender;
    private String birthDate;
    private String birthTime;
    private String bloodType;
    private String email;
}
