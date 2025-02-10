package S12P11D110.ssacle.domain.study.entity;


import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data

public class Study extends BaseEntity {
    private String id;               // MongoDB에서 자동 생성되는 고유 ID
    private String studyName;        // 스터디 이름
    private List<Topic> topic;      // 주제 목록
    private List<MeetingDays> meetingDays; // 모임 요일
    private int count;               //정원
    private Set<String> members;    // 멤버 (user의 ID)
    private String studyContent;     // 스터디 소개
    private Set<String> wishMembers; // 스카웃하고 싶은 스터디원 (user의 ID)
    private Set<String>preMembers;   // 신청한 스터디원(user의 ID)
    private Set<String> feeds;           // 스터디 피드 리스트

    // 스터디 개설일자는 BaseTimeEntity에 있음

    public enum Topic{
        cs, algorithm
    }
    public enum MeetingDays {
        MON, TUE, WED, THU, FRI, SAT, SUN
    }
}
