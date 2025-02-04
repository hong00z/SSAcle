package S12P11D110.ssacle.domain.feed.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedDetailDTO {

    private String study;       // 스터디
    private String author;      // 작성자
    private String title;       // 제목
    private String content;     // 내용

}
