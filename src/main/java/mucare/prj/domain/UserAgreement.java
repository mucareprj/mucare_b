package mucare.prj.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAgreement {
    private Long agreementId; // PK
    private Long id; // users.id (FK)

    private Boolean agreeService;
    private Boolean agreePrivacy;
    private Boolean agreeMarketing;
}
