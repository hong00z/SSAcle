package S12P11D110.ssacle.domain.feed.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedCreatorInfo {
    private String userId;
    private String nickname;
    private String image;
    private String term;
    private String campus;
}
