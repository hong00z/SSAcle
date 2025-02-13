package S12P11D110.ssacle.domain.user.controller;


import S12P11D110.ssacle.domain.auth.entity.CustomUserDetail;
import S12P11D110.ssacle.domain.study.dto.request.MyRequest;
import S12P11D110.ssacle.domain.study.dto.response.MyStudyList;
import S12P11D110.ssacle.domain.study.service.StudyService;
import S12P11D110.ssacle.domain.user.dto.request.SsafyAuthRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserProfileRequest;
import S12P11D110.ssacle.domain.user.dto.response.SsafyAuthResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserProfileResponse;
import S12P11D110.ssacle.domain.user.service.UserService;
import S12P11D110.ssacle.global.dto.ResultDto;
import S12P11D110.ssacle.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name="User Controller", description = "사용자 관련 controller (JWT ver.)")
public class UserController {
    private final UserService userService;
    private final StudyService studyService;

//------------------------------------------- << 로그아웃 & 탈퇴 >> -------------------------------------------
    /**
     * 로그아웃
     */
    @PostMapping("")
    @Operation(summary = "로그아웃", description = "사용자의 JWT 토큰을 삭제하여 로그아웃 처리")
    public ResultDto<Object> logout(@AuthenticationPrincipal CustomUserDetail userDetail) {
        try {
            userService.logout(userDetail.getId());
            return ResultDto.of(HttpStatusCode.OK, "로그아웃 성공", null);
        } catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }


    /**
     * 탈퇴
     */
    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴", description = "사용자의 JWT 토큰과 계정 정보 삭제 처리")
    public ResultDto<Object> deleteUser(@AuthenticationPrincipal CustomUserDetail userDetail) {
        userService.deleteUser(userDetail.getId());
        return ResultDto.of(HttpStatusCode.OK, "사용자 탈퇴 성공", null);
    }

//------------------------------------------- << 프로필 >> -------------------------------------------
    /**
     * 프로필 조회
     */
    @GetMapping("/profile")
    @Operation(summary = "프로필 조회", description = "프로필에 포함된 정보 : 닉네임, 프로필 사진, 기수, 캠퍼스, 스터디 관심 주제, 스터디 요일")
    public ResultDto<UserProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetail userDetail) {
        try {
            UserProfileResponse profile = userService.findUserProfile(userDetail.getId());
            return ResultDto.of(HttpStatusCode.OK, "프로필 조회 성공", profile);
        }catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/profile")
    @Operation(summary = "프로필 수정", description = "닉네임(중복 검사), 프로필 사진, 스터디 관심 주제, 스터디 요일 수정 가능")
    public ResultDto<UserProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody UserProfileRequest request){
        try {
            UserProfileResponse updatedProfile = userService.modifyUserProfile(userDetail.getId(), request);
            return ResultDto.of(HttpStatusCode.OK, "프로필 수정 성공", updatedProfile);
        } catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }

    /**
     * 닉네임 중복 검사
     */
    @GetMapping("")
    @Operation(summary="닉네임 중복 검사", description = "사용자가 화면에 입력한 닉네임이 중복되었는지 검사 (Query Parameter로 닉네임 전달)")
    public ResultDto<String> checkNickname(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestParam String nickname) {
        try {
            String currentUserNickname = userDetail.getNickname();    // 현재 로그인한 사용자 닉네임 가져오기
            boolean isDuplicated = userService.isNicknameDuplicated(nickname, currentUserNickname);
            if (isDuplicated) {
                return ResultDto.of(ApiErrorStatus.DUPLICATED_USER_NAME.getCode(), "이미 사용중인 닉네임입니다.", nickname);
            }
            return ResultDto.of(HttpStatusCode.OK, "사용 가능한 닉네임입니다.", nickname);
        } catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }

    /**
     * 싸피생 인증
     */
    @PostMapping("/ssafy")
    @Operation(summary = "싸피생 인증", description = "임시로 무조건 term=12, campus='구미'로 설정")
    public ResultDto<SsafyAuthResponse> ssafyAuth(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody SsafyAuthRequest request) {
        try {
            SsafyAuthResponse response = userService.ssafyAuth(userDetail.getId(), request);
            return ResultDto.of(HttpStatusCode.OK, "싸피생 인증 성공", response);
        } catch (AuthErrorException e) {
            return ResultDto.of(e.getCode(), e.getErrorMsg(), null);
        } catch (Exception e) {
            return ResultDto.of(HttpStatusCode.INTERNAL_SERVER_ERROR, "서버 에러", null);
        }
    }

    /**
     * 유저의 FCM 토큰 저장
     */
//    @PostMapping("/fcmTocken")
//    @Operation(summary = "유저의 FCM 토큰 저장", description = "임시로 무조건 term=12, campus='구미'로 설정"))
//    public saveFcmToken



//------------------------------------------- << 스터디 >> ------------------------------------------
    /**
     * 내 스터디 리스트
     */
    @GetMapping("/my-studies")
    @Operation(summary = "내 스터디 리스트", description = "내가 가입한 스터디 리스트를 조회합니다.")
    public List<MyStudyList> getStudiesByUserId(@AuthenticationPrincipal CustomUserDetail userDetail){
        String userId = userDetail.getId();
        return studyService.getStudiesByUserId(userId);
    }

    /**
     * 내 신청 현황
     */



    /**
     * 내 신청 보내기
     */
    //

    /**
     * 내 신청 취소
     */

    /**
     * 내 수신함
     */

    /**
     * 내 수신함 수락
     */
    // 해당 스터디의 members 에 userId 추가, wishMembers 에서 userId 삭제
    // 사용자의 invitedStudies 에서 studyId 삭제

    /**
     * 내 수신함 거절
     */


    // 내 요청함 wishStudy & 스터디 내 수신함  preMembers 추가
    @PatchMapping("/wish-studies/{studyId}") // user 로그인 정보 받아와지면 {userId} 없애기
    @Operation(summary = "스터디 가입 신청", description = "추천된 스터디에 가입 요청")
    //ResponseEntity :  HTTP 응답을 표현하는 클래스
    public ResponseEntity<Void> inviteMe(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody MyRequest request){
        String userId = userDetail.getId();  // 로그인된 사용자 ID 가져오기
        System.out.println("Received request in inviteMe method."); // 요청 도착 확인
        System.out.println("studyId: " + request.getStudyId()); // studyId 값 확인

        studyService.addWishStudyPreMember(userId, request.getStudyId());
        return ResponseEntity.ok().build();
    }



    // 해당 스터디의 wishMembers 에서 userId 삭제
    // 사용자의 invitedStudies 에서 studyId 삭제
}
