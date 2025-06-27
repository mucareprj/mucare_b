package mucare.prj.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PasswordChangeDTO {
    
    private Long userId;
    private String currentPassword;
    private String newPassword;
}
