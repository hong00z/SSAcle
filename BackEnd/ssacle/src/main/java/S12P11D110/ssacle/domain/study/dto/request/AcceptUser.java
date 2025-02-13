package S12P11D110.ssacle.domain.study.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 스터디 가입 요청한 유저를 받아드릴때 사용하는 Request Body
public class AcceptUser {
    private String userId;
}
