package S12P11D110.ssacle.domain.study.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
// invitedStudy 스카웃 요청 받은 스터디 (스터디 → 나)
public class MyInvitedStudyListDTO {
    private String userId;
    private Set<String> invitedStudy;
}
