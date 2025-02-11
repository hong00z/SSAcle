package S12P11D110.ssacle.domain.study.dto;


import lombok.Builder;
import lombok.Getter;
import java.util.Set;

@Getter
@Builder  // getter/setter, 기본 생성자, toString() 포함
// 스터디 응답 DTO
public class StudyResponseDTO {
    private String id;                          // MongoDB에서 자동 생성되는 고유 ID
    private String studyName;                   // 스터디 이름
    private String topic;            // 주제
    private Set<String> meetingDays;  // 모임 요일
    private int count;              //정원
    private Set<String> members;    // 스터디원
    private String studyContent;    // 스터디 소개
}
