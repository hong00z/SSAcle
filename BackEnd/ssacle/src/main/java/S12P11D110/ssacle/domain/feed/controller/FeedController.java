package S12P11D110.ssacle.domain.feed.controller;

import S12P11D110.ssacle.domain.feed.Service.FeedService;
import S12P11D110.ssacle.domain.feed.dto.FeedCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController //RESTful API 개발에 사용
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "feed Controller", description = "This is Feed Controller")
public class FeedController {

    private final FeedService feedService;

    // 피드 생성
    @PostMapping("/studies/{studyId}/{userId}/feed") // 로그인 구현 만들어지면 {userId} 삭제
    @Operation(summary = "피드 생성", description = "피드를 생성합니다.")
    // 로그인 구현 만들어지면 userId 삭제
    public ResponseEntity<Void> saveFeed (@PathVariable String studyId, String userId, @RequestBody FeedCreateDTO feedCreateDTO){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        feedService.saveFeed(studyId, userId, feedCreateDTO);
        return ResponseEntity.ok().build();
    }
}
