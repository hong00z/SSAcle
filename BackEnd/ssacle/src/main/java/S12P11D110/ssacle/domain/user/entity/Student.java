package S12P11D110.ssacle.domain.user.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@Document(collection = "students")
public class Student {
    @Id
    private String id;          // MongoDB 기본 키 (_id)

    private String name;        // 이름
    private int studentId;   // 학번
    private String term;        // 기수
    private String campus;      // 캠퍼스
    private String userId;      // 인증된 사용자의 ID (없으면 null)
}
