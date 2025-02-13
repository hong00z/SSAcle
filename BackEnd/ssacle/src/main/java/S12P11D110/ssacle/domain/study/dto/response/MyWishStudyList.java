package S12P11D110.ssacle.domain.study.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// wishStudy 신청한 스터디 리스트: 나 -> 스터디
public class MyWishStudyList {
    private String userId;
    private List<WishInvitedStudies> wishStudy;

}
