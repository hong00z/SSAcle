package S12P11D110.ssacle.domain.study.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
// 내가 참여중인 스터디 - 멤버 - 멤버 닉네임/ 이미지
public class nicknameImage {
    private String nickname;
    private String image;
    private boolean isCreator;
}
