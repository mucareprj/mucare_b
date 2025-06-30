package mucare.prj.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    

    private String email;
    private String password;
    private String phoneNumber;

    private Boolean agreeService;
    private Boolean agreePrivacy;
    private Boolean agreeMarketing;

}
