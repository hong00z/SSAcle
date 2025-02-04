package S12P11D110.ssacle.domain.study.dto;

import S12P11D110.ssacle.domain.study.entity.Study;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data // getter/setter, 기본 생성자, toString() 포함

// 스터디 생성 요청 DTO
public class StudyCreateRequestDTO {

    private String studyName;                   // 스터디 이름
    private List<Study.Topic> topic;            // 주제 목록
    private List<Study.MeetingDays> meetingDays;  // 모임 요일
    private int count;                          //정원
    private String studyContent;                // 스터디 소개
    private LocalDateTime createdAt;            // 스터디 개설일자

}
