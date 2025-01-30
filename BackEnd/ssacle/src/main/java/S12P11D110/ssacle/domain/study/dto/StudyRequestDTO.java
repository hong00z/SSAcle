package S12P11D110.ssacle.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

// 스카웃 요청 신청 Patch 용 request body DTO
public class StudyRequestDTO {
    private String userId;
}
