package S12P11D110.ssacle.domain.study.dto.response;

import S12P11D110.ssacle.domain.study.dto.StudyDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// wishStudy 신청한 스터디 리스트: 나 -> 스터디
public class MyWishStudyListDTO {
    private String userId;
    private List<StudyDTO> wishStudy;

}
