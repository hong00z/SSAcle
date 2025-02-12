package S12P11D110.ssacle.domain.tempUser;


import S12P11D110.ssacle.domain.study.dto.response.MyInvitedStudyListDTO;
import S12P11D110.ssacle.domain.study.dto.response.MyWishStudyListDTO;
import S12P11D110.ssacle.domain.study.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("tempUserController") //RESTful API 개발에 사용
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "TempUser Controller", description = "사용자 관련 임시 controller (문경 ver.)")
public class TempUserController {

    private final StudyService studyService;
    private final TempUserService tempUserService;

//--------------------<<  내 수신함   >>---------------------------------------------------------------------------------
    // wishStudy 신청한 스터디 리스트: 나 -> 스터디
    @GetMapping("/{userId}/wishList") //로그인 기능 만들어지면 /{userId} 없애기
    @Operation(summary = "wishStudy 리스트 조회", description = "wishStudy 신청한 스터디 리스트: 나 -> 스터디")
    //로그인 기능 만들어지면 @PathVariable String userId) 없애기
    public List<MyWishStudyListDTO> getWishStudyList (@PathVariable String userId){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        return studyService.myWishStudyList(userId);
    }


    // invitedStudy 스카웃 요청 받은 스터디 (스터디 → 나)
    @GetMapping("/{userId}/invitedList") //로그인 기능 만들어지면 /{userId} 없애기
    @Operation(summary = "invitedStudy 리스트 조회", description = "invitedStudy 신청한 스터디 리스트: 스터디 -> 나")
    //로그인 기능 만들어지면 @PathVariable String userId) 없애기
    public List<MyInvitedStudyListDTO> getInvitedStudyList(@PathVariable String userId){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        return studyService.myInvitedStudyList(userId);
    }


    // 유저: 스터디의 요청 수락
    @PatchMapping("/{userId}/invitedList/{studyId}/accept") //로그인 기능 만들어지면 /{userId} 없애기
    @Operation(summary = "가입 요청 수락", description = "joinedStudy와 members에 스터디 추가")
    //로그인 기능 만들어지면 @PathVariable String userId) 없애기
    public ResponseEntity<Void> acceptInvite (@PathVariable String userId, String studyId){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        studyService.addJoinedStudyMember(userId, studyId);
        studyService.editInvitedStudyWishMembers(userId, studyId);
        return ResponseEntity.ok().build();
    }


//----------------------------------------------------------------------------------------------------------------------

    // 유저의 프로필 수정
    @PatchMapping(value = "/{userId}/profile",
            // 클라이언트가 보내는 데이터 타입 ()
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,  //MULTIPART_FORM_DATA_VALUE = JSON과 파일을 동시에 전송할 때 사용하는 형식
            // 서버가 클라이언트에게 보내는 응답 타입
            produces = MediaType.APPLICATION_JSON_VALUE // APPLICATION_JSON_VALUE = JSON 형태의 응답을 보낼 것을 명시 )
    )
    @Operation(summary = "프로필 수정", description = "프로필 이미지 수정 추가")
    public ResponseEntity<UserProfileResponse> userModify(
            @PathVariable("userId") String userId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestPart TempUserProfileRequest request,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = "multipart/form-data"))
            @RequestParam(value = "MultipartFile", required = false) MultipartFile file){
        return ResponseEntity.ok(tempUserService.modifyUserProfile(userId, request, file));
    }



}
