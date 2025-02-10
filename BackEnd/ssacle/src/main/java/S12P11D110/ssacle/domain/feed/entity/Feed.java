package S12P11D110.ssacle.domain.feed.entity;

import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "feeds")
public class Feed extends BaseEntity {
    private String id;      // 피드ID
    private String study;   // 스터디
    private String author;  // 작성자 : 로그인 기능 완성되면 뺴야함
    private String title;    // 제목
    private String content; // 내용

    // 작성일, 수정일: BaseEntity

}
