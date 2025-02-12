package S12P11D110.ssacle.domain.study.entity;


import S12P11D110.ssacle.global.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Set;


@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "studies")
public class Study extends BaseEntity {
    @Id
    private String id;                  // MongoDB에서 자동 생성되는 고유 ID
    private String studyName;           // 스터디 이름
    @Field("topic")
    private String topic;               // 주제 목록
    private Set<String> meetingDays;    // 모임 요일
    private int count;                  //정원
    private Set<String> members;        // 멤버 (user의 ID)
    private String studyContent;        // 스터디 소개
    private Set<String> wishMembers;    // 스카웃하고 싶은 스터디원 (user의 ID)
    private Set<String>preMembers;      // 신청한 스터디원(user의 ID)

    // 스터디 개설일자는 BaseTimeEntity에 있음

//    public enum Topic{
//        cs, algorithm
//    }
//    public enum MeetingDays {
//        MON, TUE, WED, THU, FRI, SAT, SUN
//    }
}
