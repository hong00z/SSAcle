package S12P11D110.ssacle.domain.study.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// invitedStudy 스카웃 요청 받은 스터디 (스터디 → 나)
public class MyInvitedStudyList {
    private String userId;
    private List<WishInvitedStudies> invitedStudy;
}
