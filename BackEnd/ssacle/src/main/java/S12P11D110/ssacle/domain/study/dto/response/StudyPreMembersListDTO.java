package S12P11D110.ssacle.domain.study.dto.response;

import S12P11D110.ssacle.domain.tempUser.SearchUserDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// preMembers 신청한 스터디원 (사용자→ 내 스터디)
public class StudyPreMembersListDTO {
    private String studyId;
    private List<SearchUserDTO> preMembers;
}
