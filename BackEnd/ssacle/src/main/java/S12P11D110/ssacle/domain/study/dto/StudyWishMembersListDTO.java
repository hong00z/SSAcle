package S12P11D110.ssacle.domain.study.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
// wishMembers 스카웃하고 싶은 스터디원 (내 스터디 → 사용자)
public class StudyWishMembersListDTO {
    private String studyId;
    private Set<String> wishMembers;
}
