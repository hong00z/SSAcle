package S12P11D110.ssacle.domain.user.dto;


import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.global.entity.MeetingDay;
import S12P11D110.ssacle.global.entity.Topic;
import lombok.*;

import java.util.Set;

// GPT 도움!! (전체)
@Getter
@Builder
@AllArgsConstructor // 명시적으로 public 생성자 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 new UserDTO() 방지
public class UserDTO {
    private String userId;
    private String email;
    private String nickname;
    private String image;

    private String term;
    private String campus;

    private Set<Topic> topics;
    private Set<MeetingDay> meetingDays;

    private Set<String> joinedStudies;
    private Set<String> wishStudies;
    private Set<String> invitedStudies;
//    @Setter
//    private String refreshToken;


    // 엔티티를 DTO로 변환하는 생성자 추가
    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.image = user.getImage();
        this.term = user.getTerm();
        this.campus = user.getCampus();
        this.topics = user.getTopics();
        this.meetingDays = user.getMeetingDays();
        this.joinedStudies = user.getJoinedStudies();
        this.wishStudies = user.getWishStudies();
        this.invitedStudies = user.getInvitedStudies();
    }
}
