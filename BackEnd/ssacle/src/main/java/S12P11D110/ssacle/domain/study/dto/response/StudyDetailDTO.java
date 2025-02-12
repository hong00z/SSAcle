package S12P11D110.ssacle.domain.study.dto.response;

import S12P11D110.ssacle.domain.feed.dto.FeedDetailDTO;
import S12P11D110.ssacle.domain.study.dto.nicknameImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder  // getter/setter, 기본 생성자, toString() 포함
public class StudyDetailDTO {
    private String id;                          // MongoDB에서 자동 생성되는 고유 ID
    private String studyName;                   // 스터디 이름
    private String topic;                   // 주제 목록
    private Set<String> meetingDays;        // 모임 요일
    private int count;                      //정원
    private int memberCont;                 // 현 멤버 수
    private List<nicknameImage> members;    // 스터디원
    private String studyContent;            // 스터디 소개
    private List<FeedDetailDTO> feeds;      // 피드
    private String createdBy;



}
