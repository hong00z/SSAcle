package S12P11D110.ssacle.domain.user.controller;


import S12P11D110.ssacle.domain.auth.entity.CustomUserDetail;
import S12P11D110.ssacle.domain.study.dto.request.MyRequest;
import S12P11D110.ssacle.domain.study.dto.response.MyStudyList;
import S12P11D110.ssacle.domain.study.dto.response.WishInvitedStudies;
import S12P11D110.ssacle.domain.study.service.StudyService;
import S12P11D110.ssacle.domain.user.dto.request.NicknameRequest;
import S12P11D110.ssacle.domain.user.dto.request.SsafyAuthRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserProfileRequest;
import S12P11D110.ssacle.domain.user.dto.response.SsafyAuthResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserProfileResponse;
import S12P11D110.ssacle.domain.user.service.UserService;
import S12P11D110.ssacle.global.dto.ResultDto;
import S12P11D110.ssacle.global.exception.*;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.multipart.MultipartFile;

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
    public ResultDto<Object> logout(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestHeader("Refresh-Token") String refreshToken) {
        try {
            userService.logout(userDetail.getId(), refreshToken);
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
    @Operation(summary = "회원 탈퇴", description = "사용자의 JWT 토큰과 계정 정보 삭제하여 탈퇴 처리")
    public ResultDto<Object> deleteUser(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestHeader("Refresh-Token") String refreshToken) {
        userService.deleteUser(userDetail.getId(), refreshToken);
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
    @PatchMapping(value = "/profile",
            // 클라이언트가 보내는 데이터 타입 ()
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,  //MULTIPART_FORM_DATA_VALUE = JSON과 파일을 동시에 전송할 때 사용하는 형식
            // 서버가 클라이언트에게 보내는 응답 타입
            produces = MediaType.APPLICATION_JSON_VALUE // APPLICATION_JSON_VALUE = JSON 형태의 응답을 보낼 것을 명시 )
    )
    @Operation(summary = "프로필 수정", description = "닉네임(중복 검사), 프로필 사진, 스터디 관심 주제, 스터디 요일 수정 가능")
    public ResponseEntity<UserProfileResponse> userModify(
            @AuthenticationPrincipal CustomUserDetail userDetail,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestPart UserProfileRequest request,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam(value = "MultipartFile", required = false) MultipartFile file){
        String userId = userDetail.getId();
        return ResponseEntity.ok(userService.modifyUserProfile(userId, request, file));
    }

    /**
     * 닉네임 중복 검사
     */
    @PostMapping("/nickname")
    @Operation(summary="닉네임 중복 검사", description = "사용자가 화면에 입력한 닉네임이 중복되었는지 검사")
    public ResultDto<Boolean> checkNickname(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody NicknameRequest request) {
        try {
            String currentUserNickname = userDetail.getNickname();    // 현재 로그인한 사용자 닉네임 가져오기
            boolean isDuplicated = userService.isNicknameDuplicated(request.getNickname(), currentUserNickname);
            if (isDuplicated) {
                return ResultDto.of(ApiErrorStatus.DUPLICATED_USER_NAME.getCode(), "이미 사용중인 닉네임입니다.", true);
            }
            return ResultDto.of(HttpStatusCode.OK, "사용 가능한 닉네임입니다.", false);
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
    @GetMapping("/wish-studies")
    @Operation(summary = "내 신청 현황 리스트", description = "내가 신청한 스터디 리스트를 조회합니다.")
    public List<WishInvitedStudies> getWishStudyList (@AuthenticationPrincipal CustomUserDetail userDetail){
        String userId = userDetail.getId();
        return studyService.getWishStudyList(userId);
    }

    /**
     * 내 신청 보내기
     */
    @PatchMapping("/wish-studies")
    @Operation(summary = "내 신청 보내기", description = "스터디에 가입 신청할 수 있습니다.")
    //ResponseEntity :  HTTP 응답을 표현하는 클래스
    public ResponseEntity<Void> inviteMe(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody MyRequest request){
        String userId = userDetail.getId();  // 로그인된 사용자 ID 가져오기
        System.out.println("Received request in inviteMe method."); // 요청 도착 확인
        System.out.println("studyId: " + request.getStudyId()); // studyId 값 확인
        studyService.addWishStudyPreMember(userId, request.getStudyId());
        return ResponseEntity.ok().build();
    }

    /**
     * 내 신청 취소
     */
    // 해당 스터디의 preMembers 에서 userId 삭제
    // 사용자의 wishStudies 에서 studyId 삭제


    /**
     * 내 수신함
     */
    @GetMapping("/invited-studies")
    @Operation(summary = "내 수신함", description = "내가 초대 받은 스터디 리스트를 조회합니다.")
    public List<WishInvitedStudies> getInvitedStudyList(@AuthenticationPrincipal CustomUserDetail userDetail){
        String userId = userDetail.getId();
        return studyService.getInvitedStudyList(userId);
    }

    /**
     * 내 수신함 수락
     */
    @PatchMapping("/invited-studies/accept")
    @Operation(summary = "내 수신함 수락", description = "내가 초대 받은 스터디의 요청을 수락합니다.")
    public ResponseEntity<Void> accetpInvite(@AuthenticationPrincipal CustomUserDetail userDetail, @RequestBody MyRequest request){
        String userId = userDetail.getId();  // 로그인된 사용자 ID 가져오기
        // 유저: joinedStudies 추가 / 스터디: members에 추가
        studyService.addJoinedStudyMember(userId, request.getStudyId());
        // 유저: invitedStudies에 제거 / 스터디: wishMembers에 제거
        studyService.editInvitedStudyWishMembers(userId, request.getStudyId());//
        return ResponseEntity.ok().build();
    }

    /**
     * 내 수신함 거절
     */
    // 해당 스터디의 wishMembers 에서 userId 삭제
    // 사용자의 invitedStudies 에서 studyId 삭제


//------------------------------------------- << FCM 토큰 >> ------------------------------------------
    /**
     * 유저의 FCM 토큰 저장
     */
//    @PostMapping("/fcmTocken")
//    @Operation(summary = "유저의 FCM 토큰 저장", description = "임시로 무조건 term=12, campus='구미'로 설정"))
//    public saveFcmToken

}
