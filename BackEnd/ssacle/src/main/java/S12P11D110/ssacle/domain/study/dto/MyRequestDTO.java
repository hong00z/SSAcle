package S12P11D110.ssacle.domain.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

// 스터디 가입 신청 Patch 용 request body DTO
public class MyRequestDTO {
    private String studyId;
}
