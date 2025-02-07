package S12P11D110.ssacle.domain.user.controller;


import S12P11D110.ssacle.domain.user.dto.request.SsafyAuthRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserProfileRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserRegisterRequest;
import S12P11D110.ssacle.domain.user.dto.response.SsafyAuthResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserProfileResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserRegisterResponse;
import S12P11D110.ssacle.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name="User Controller", description = "사용자 관련 controller (민주 ver.)")
public class UserController {
    private final UserService userService;

    //------------------------------------------- << 임시 회원가입 >> -------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

//------------------------------------------- << 프로필 >> -------------------------------------------
    /**
     * 싸피생 인증
     */
    @PostMapping("/{userId}/ssafy")
    @Operation(summary = "싸피생 인증", description = "임시로 무조건 term=12, campus='구미'로 설정")
    public ResponseEntity<SsafyAuthResponse> ssafyAuth(@PathVariable("userId") String userId, @RequestBody SsafyAuthRequest request) {
        return ResponseEntity.ok(userService.ssafyAuth(userId, request));
    }

    /**
     * 닉네임 중복 검사
     */
    @GetMapping("/{nickname}")
    @Operation(summary="닉네임 중복 검사", description = "사용자가 화면에 입력한 닉네임이 중복되었는지 검사")
    public ResponseEntity<String> checkNickname(@PathVariable("nickname") String nickname){
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }

    /**
     * (카카오 로그인 후) 프로필 생성
     */
    @PostMapping("/{userId}/profile")
    @Operation(summary = "프로필 생성", description = "관심 주제, 스터디 요일 생성")
    public ResponseEntity<UserProfileResponse> createProfile(@PathVariable("userId") String userId, @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.createUserProfile(userId, request));
    }

    /**
     * 프로필 조회
     */
    @GetMapping("/{userId}/profile")
    @Operation(summary = "프로필 조회", description = "프로필에 포함된 정보 : 닉네임, 프로필 사진, 기수, 캠퍼스, 관심 주제, 요일")
    public ResponseEntity<UserProfileResponse> userProfile(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.findUserProfile(userId));
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/{userId}/profile")
    @Operation(summary = "프로필 수정", description = "닉네임(중복 검사 선행), 프로필 사진, 관심 주제, 요일 수정 가능")
    public ResponseEntity<UserProfileResponse> userModify(@PathVariable("userId") String userId, @RequestBody UserProfileRequest request){
        return ResponseEntity.ok(userService.modifyUserProfile(userId, request));
    }



//------------------------------------------- << 스터디 >> ------------------------------------------
    // 참여중인 스터디 목록 조회

    // 신청한 스터디 목록 조회

    // 초대 받은 스터디 수락
    // 해당 스터디의 members 에 userId 추가, wishMembers 에서 userId 삭제
    // 사용자의 invitedStudies 에서 studyId 삭제

    // 초대 받은 스터디 거절
    // 해당 스터디의 wishMembers 에서 userId 삭제
    // 사용자의 invitedStudies 에서 studyId 삭제
}
