package S12P11D110.ssacle.domain.study.dto.response;


import S12P11D110.ssacle.domain.tempUser.SearchUserDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// wishMembers 스카웃하고 싶은 스터디원 (내 스터디 → 사용자)
public class StudyWishMembersListDTO {
    private String studyId;
    private List<SearchUserDTO> wishMembers;
}
