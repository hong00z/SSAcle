package S12P11D110.ssacle.domain.study.controller;

import S12P11D110.ssacle.domain.study.dto.request.MyRequestDTO;
import S12P11D110.ssacle.domain.study.dto.request.StudyCreateRequestDTO;
import S12P11D110.ssacle.domain.study.dto.request.StudyRequestDTO;
import S12P11D110.ssacle.domain.study.dto.response.*;
import S12P11D110.ssacle.domain.study.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController //RESTful API 개발에 사용
@RequiredArgsConstructor
@RequestMapping("/api/studies")
@Tag(name = "Study Controller", description = "This is Study Controller")
public class StudyController {

    private final StudyService studyService;


    @GetMapping("/create")
    @Operation(summary = "스터디 주제, 모임목록 ", description = "새로운 스터디를 개설합니다.")
    public Map<String, List<String>> getTopicsMeetingDays(){
        return studyService.topicList();
    }

    // GPT: 25 ~31
    // 스터디 생성 POST
    @PostMapping("/{userId}") // 로그인 기능 만들어지면 ("/{userId}") 이 부분 없애기
    @Operation(summary = "스터디 개설", description = "새로운 스터디를 개설합니다.")
    // 로그인 기능 만들어지면 @PathVariable String userId 이 부분 없애기
    public ResponseEntity<Void> createStudy(@PathVariable String userId, @RequestBody StudyCreateRequestDTO studyCreateRequestDTO){  // 클라이언트로부터 전달받은 JSON 형식의 데이터를 @RequestBody를 통해 Java의 객체(StudyCreateRequestDTO)로 자동 변환
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        studyService.saveStudy(userId, studyCreateRequestDTO);
        return ResponseEntity.ok().build();
    }

    // 전체 스터디 조회 GET
    @GetMapping
    @Operation(summary = "모든 스터디 조회", description = "등록된 모든 스터디를 조회합니다.")
    public List<StudyResponseDTO> getAllStudies(){

        return studyService.getAllStudy();
    }


    // GTP : 37 ~ 46
//    // 해당 조건의 스터디 그룹 조회
//    @GetMapping
//    @Operation(summary = "조건부 스터디 조회", description = "주제와 모임 요일 조건에 따라 스터디를 조회합니다. 조건이 없으면 전체 스터디를 반환합니다.")
//    public List<StudyResponseDTO> getStudiesByConditions(
//            @RequestParam(required = false) List<Study.Topic> topic, // 쿼리 파라미터 topic
//            @RequestParam(required = false) List<Study.MeetingDay> meetingDay // 쿼리 파라미터 meetingDay
//    ) {
//        return studyService.getStudiesByConditions(topic, meetingDay);
//    }


    // 스터디 상세보기 GET
    @GetMapping("/{studyId}")
    @Operation(summary = "특정 스터디 조회", description = "스터디 ID를 통해 특정 스터디를 조회합니다.")
    public StudyDetailDTO getstudyById(@PathVariable String studyId){
        return studyService.getStudyById(studyId);
    }


    // 내가 참여중인 스터디 리스트 GET
    // user 로그인 정보 받아와지면 {userId} >> users로 수정해야함
    @GetMapping("/{userId}/my-studies")
    @Operation(summary = "내가 참여 중인 스터디 리스트 조회", description = "TempUser Id를 통해 해당 User가 가입한 스터디 리스트를 조회합니다.")
    public List<StudyResponseDTO> getStudiesByUserId(@PathVariable String userId){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        return studyService.getStudiesByUserId(userId);

    }


//-------------------<< 스터디원 추천 기능>>-------------------------------------------------------------------------------
    // 스터디원 추천 기능
    @GetMapping("/recommendUser/{studyId}")
    @Operation(summary = "스터디원 추천", description = "스터디에 적합한 상위 3명의 유저 리스트를 제공합니다.")
    public List<RecommendUserDTO>getRecommendUser(@PathVariable String studyId){
        return studyService.getStudyCondition(studyId);
    }

    // 스터디내 초대 현황 wishMembers & 내 수신함  invitedStudy 추가
    @PatchMapping("/{studyId}/addStudyRequest")
    @Operation(summary = "스터디원 스카웃 제의 추가/ 내 수신함 추가", description = "추천된 유저에게 스카웃 제의에 추가")
    //ResponseEntity :  HTTP 응답을 표현하는 클래스
    public ResponseEntity<Void> comeToStudy(@PathVariable String studyId, @RequestBody StudyRequestDTO request){
        studyService.addWishMemberInvitedStudy(studyId, request.getUserId());
        return ResponseEntity.ok().build();
    }
//----------------------------------------------------------------------------------------------------------------------



//-------------------<< 스터디 추천 기능>>-------------------------------------------------------------------------------
    // 스터디 추천기능
    @GetMapping("recommendStudy/{userId}") // user 로그인 정보 받아와지면 {userId} 없애기
    @Operation(summary = "스터디 추천", description = "유저에게 적합한 상위 3개의 스터디 리스트를 제공합니다.")
    public List<RecommendStudyDTO> getRecommendStudy(@PathVariable String userId){
        return studyService.getUserCondition(userId);
    }

    // 내 요청함 wishStudy & 스터디 내 수신함  preMembers 추가
    @PatchMapping("/{userId}/addMyRequest") // user 로그인 정보 받아와지면 {userId} 없애기
    @Operation(summary = "내 요청함 추가 / 스터디 내 수신함 추가", description = "추천된 스터디에 가입 요청")
    //ResponseEntity :  HTTP 응답을 표현하는 클래스
    // 로그인 기능 완성되면 @PathVariable String userId 없애기
    public ResponseEntity<Void> inviteMe(@PathVariable String userId, @RequestBody MyRequestDTO request){
//        String userId = authentication.getName();  // 로그인된 사용자 ID 가져오기
        System.out.println("Received request in inviteMe method."); // 요청 도착 확인
        System.out.println("studyId: " + request.getStudyId()); // studyId 값 확인

        studyService.addWishStudyPreMember(userId, request.getStudyId());
        return ResponseEntity.ok().build();
    }
//----------------------------------------------------------------------------------------------------------------------



//--------------------<<  스터디 수신함   >>------------------------------------------------------------------------------
    @GetMapping("/{studyId}/wishList") //
    @Operation(summary = "StudyWishMembers 리스트 조회", description = "wishMembers 스카웃하고 싶은 스터디원 : 내 스터디 → 사용자")
    public List<StudyWishMembersListDTO> getStudyWishMembersList (String studyId){
        return studyService.studyWishMembersList(studyId);
    }

    @GetMapping("/{studyId}/preList") //
    @Operation(summary = "StudyPreMemberList 리스트 조회", description = "// preMembers 신청한 스터디원: 사용자→ 내 스터디")
    public List<StudyPreMembersListDTO> getStudyPreMembersList(String studyId){
        return studyService.studyPreMembersList(studyId);
    }

//----------------------------------------------------------------------------------------------------------------------

    // 스터디: 유저의 요청 수락
    @PatchMapping("/{studyId}/{userId}/accept")
    @Operation(summary = "가입 요청 수락", description = "joinedStudy와 members에 스터디 추가")
    public ResponseEntity<Void> acceptInvite (@PathVariable String userId, String studyId){
        studyService.addJoinedStudyMember(userId, studyId);
        studyService.editWishStudyPreMembers(userId, studyId);
        return ResponseEntity.ok().build();
    }










    // 스터디 수정
    // 스터디 삭제




}
