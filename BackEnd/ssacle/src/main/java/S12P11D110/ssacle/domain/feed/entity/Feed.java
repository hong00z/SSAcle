package S12P11D110.ssacle.domain.feed.entity;

import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.Data;

@Data
public class Feed extends BaseEntity {
    private String id;      // 피드ID
    private String study;   // 스터디
    private String author;  // 작성자
    private String title;    // 제목
    private String content; // 내용

    // 작성일, 수정일: BaseEntity

}
