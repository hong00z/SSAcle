package S12P11D110.ssacle.domain.feed.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@Getter
@Setter
public class FeedDetailDTO {

    private String study;                       // 스터디
    private FeedCreatorInfo creatorInfo;        // 작성자 정보
    private String title;                       // 제목
    private String content;                     // 내용
    private LocalDateTime createdAt;               // 작성 시간

    public FeedDetailDTO(String study, FeedCreatorInfo creatorInfo, String title, String content, LocalDateTime createdAt){
        this.study = study;
        this.creatorInfo = creatorInfo;
        this.title  = title ;
        this.content = content;
        this.createdAt = createdAt;
    }
}
