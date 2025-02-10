package S12P11D110.ssacle.global.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document // GPT 도움!! (JPA 대신 MongoDB 어노테이션으로 변경)
public abstract class BaseEntity {

    // Entity 생성 시간 자동 저장
    @CreatedDate
    private LocalDateTime createdAt;

    // Entity 변경 시간 자동 저장
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
