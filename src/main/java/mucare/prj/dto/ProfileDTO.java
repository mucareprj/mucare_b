package mucare.prj.dto;

import org.springframework.web.multipart.MultipartFile;

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
    private String profileImage;

        private MultipartFile image;  // 이미지 파일 필드 추
}
