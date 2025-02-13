package S12P11D110.ssacle.domain.study.dto.request;


import lombok.Data;
import java.util.Set;


@Data // getter/setter, 기본 생성자, toString() 포함

// 스터디 생성 요청 DTO
public class StudyCreateRequest {

    private String studyName;                   // 스터디 이름
    private String topic;                   // 주제 목록
    private Set<String> meetingDays;        // 모임 요일
    private int count;                          //정원
    private String studyContent;                // 스터디 소개


}
