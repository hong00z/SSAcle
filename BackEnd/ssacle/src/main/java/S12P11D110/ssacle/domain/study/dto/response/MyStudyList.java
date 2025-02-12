package S12P11D110.ssacle.domain.study.dto.response;

import S12P11D110.ssacle.domain.study.dto.nicknameImage;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class MyStudyList {
    private String id;                          // MongoDB에서 자동 생성되는 고유 ID
    private String studyName;                   // 스터디 이름
    private String topic;                       // 주제
    private Set<String> meetingDays;            // 모임 요일
    private int count;                          //정원
    private int memberCount;                   // 현 멤버 수
    private List<nicknameImage> members;        // 멤버들의 닉네임, 프로필 사진
    private String studyContent;                // 스터디 소개
    private String createdBy;                   // 방장


}
