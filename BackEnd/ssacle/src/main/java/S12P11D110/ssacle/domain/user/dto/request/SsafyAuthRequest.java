package S12P11D110.ssacle.domain.user.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SsafyAuthRequest {
    private String name;    // 본명
    private int studentId;  // 싸피 학번
}
