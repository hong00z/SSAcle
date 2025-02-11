package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.SsacleApplication;
import S12P11D110.ssacle.domain.feed.dto.FeedDetailDTO;
import S12P11D110.ssacle.domain.feed.entity.Feed;
import S12P11D110.ssacle.domain.feed.repository.FeedRepository;
import S12P11D110.ssacle.domain.study.dto.*;
import S12P11D110.ssacle.domain.study.dto.request.StudyCreateRequestDTO;
import S12P11D110.ssacle.domain.study.dto.request.StudyUpdateRequestDTO;
import S12P11D110.ssacle.domain.study.dto.response.*;
import S12P11D110.ssacle.domain.study.entity.Study;
import S12P11D110.ssacle.domain.tempUser.TempUser;
import S12P11D110.ssacle.domain.study.repository.StudyRepository;
import S12P11D110.ssacle.domain.tempUser.SearchUserDTO;
import S12P11D110.ssacle.domain.tempUser.TempUserRepository;
import S12P11D110.ssacle.global.firebase.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor // 생성자 주입
public class StudyService {

    private final StudyRepository studyRepository; //final이 붙은 필드나 @NonNull로 선언된 필드에 대해 생성자를 자동으로 생성해주는 기능
    private final TempUserRepository userRepository;
    private final RecommendUserService recommendUserService;
    private final RecommendStudyService recommendStudyService;
    private final FeedRepository feedRepository;
    private final FirebaseMessagingService firebaseMessagingService;

    // 스터디 토픽 리스트 & 모임요일 리스트 반환
    public Map<String, List<String>> topicList() {
        return Map.of(
                "topics", Arrays.asList(SsacleApplication.Topics),
                "meetingDays", Arrays.asList(SsacleApplication.MeetingDays)
        );
    }


    // 스터디 개설
    //트랜잭션을 시작, 커밋, 롤백하는 과정을 자동으로 관리
    public void saveStudy(String userId, StudyCreateRequestDTO studyCreateRequestDTO) {
        Study study = new Study();
        study.setStudyName(studyCreateRequestDTO.getStudyName());
        study.setTopic(studyCreateRequestDTO.getTopic());
        study.setMeetingDays(studyCreateRequestDTO.getMeetingDays());
        study.setCount(studyCreateRequestDTO.getCount());
        study.setStudyContent(studyCreateRequestDTO.getStudyContent());

        // members: 현재 스터디 개설자의 userId가 들어가야함
        Set<String> members = new HashSet<>();
        members.add(userId);
        study.setMembers(members);

        // WishMembers, PreMember는 개설 단계에서 입력하지 않음
        study.setWishMembers(new HashSet<>());
        study.setPreMembers(new HashSet<>());

        studyRepository.save(study);

    }

    //gpt: from
    // 모든 스터디 조회
    @Transactional(readOnly = true)
    public List<StudyResponseDTO> getAllStudy() {
        //1. repository에서 엔티티 가져오기
        List<Study> studies = studyRepository.findAll();

        //2. stream api를 사용해서 DTO로 변환
        return studies.stream()
                .map(study -> StudyResponseDTO.builder()
                        .id(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .studyContent(study.getStudyContent())
                        .build()
                )
                .collect(Collectors.toList());

    }
    // gpt: to

    //gpt: from
    //해당 조건의 스터디 그룹 조회
    public List<StudyResponseDTO> getStudiesByConditions(Set<String> topics, Set<String> meetingDays) {
        List<Study> studies;

        // 조건이 없으면 전체 스터디 조회
        if ((topics == null || topics.isEmpty()) && (meetingDays == null || meetingDays.isEmpty())) {
            studies = studyRepository.findAll();
        }
        // 조건이 있으면 해당 조건에 맞는 스터디 조회
        else {
            studies = studyRepository.findByTopicAndMeetingDaysIn(topics, meetingDays);
        }
        // DTO로 변환
        return studies.stream()
                .map(study -> StudyResponseDTO.builder()
                        .id(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .studyContent(study.getStudyContent())
                        .build()
                )
                .collect(Collectors.toList());
    }
    //gpt:to

    // gpt: from
    // 스터디 상세보기
    @Transactional(readOnly = true)
    public StudyDetailDTO getStudyById(String studyId) {
        // 1. 해당 스터디 찾기, 스터디에 해당하는 피드들 찾기
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID " + studyId + "에 해당하는 스터디가 없습니다."));


        //2. 스터디에 해당 하는 feed, member 찾기
        // 스터디에 해당하는 feed 모으기
        List<Feed> feedEntities= feedRepository.findByStudy(studyId);
        if(feedEntities.isEmpty()){
            feedEntities = new ArrayList<>() {
            };
        }
        // 스터디 멤버들 Id 모으기, 실제 TempUser 엔티티 조회
        Set<String> userIds = study.getMembers();
        List<TempUser> tempUserEntities = userRepository.findAllById(userIds);

        // 3. 조회된 피드, 유저를 feedList, nikcknameList 변환
        // gpt: from ---------------------------------------------------------
        List<FeedDetailDTO> feedList = feedEntities.stream()
                .map(feed ->{
                    // 작성자의 ID 가져오기
                    String userId = feed.getAuthor();

                    // userId를 이용해 해당 유저 찾기
                    String nickname = userRepository.findById(userId)
                            .map(TempUser::getNickname)
                            .orElse("Unknown"); // 유저가 없으면 기본값 설정
                    //DTO 변환
                    return FeedDetailDTO.builder()
                            .study(feed.getStudy())
                            .author(nickname)
                            .title(feed.getTitle())
                            .content(feed.getContent())
                            .build();
                })
                .collect(Collectors.toList());
        // gpt: to ---------------------------------------------------------

        // 스터디 가입 멤버 이름 리스트
        List<String> nikcknameList = tempUserEntities.stream()
                .map(TempUser::getNickname)
                .collect(Collectors.toList());

        // 4. StudyResponseDTO반환
        return StudyDetailDTO.builder()
                .id(study.getId())
                .studyName(study.getStudyName())
                .topic(study.getTopic())
                .meetingDays(study.getMeetingDays())
                .count(study.getCount())
                .members(nikcknameList)
                .studyContent(study.getStudyContent())
                .feeds(feedList)
                .build();

    }
    // gpt:to

    // 내가 참여중인 스터디 리스트
    @Transactional(readOnly = true)
    public List<StudyResponseDTO> getStudiesByUserId(String userId) {
        return studyRepository.findByMembersContaining(userId).stream()
                .map(study -> StudyResponseDTO.builder()
                        .id(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .studyContent(study.getStudyContent())
                        .build()

                )
                .collect(Collectors.toList());
    }

    //-------------------<< 스터디원 추천 기능>>-------------------------------------------------------------------------------
    //GPT: from
    // 스터디 주제, 모임 요일 정보 요약
    @Transactional(readOnly = true)
    public List<RecommendUserDTO> getStudyCondition(String studyId) {
        // 1. 스터디 조건
        StudyConditionDTO studyCondition = studyRepository.findById(studyId)
                .map(study -> StudyConditionDTO.builder()
                        .id(study.getId())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .build())
                .orElseThrow(() -> new NoSuchElementException("스터디 ID " + studyId + "에 해당하는 스터디가 존재하지 않습니다."));

        System.out.println("Study Condition: " + studyCondition); // 디버깅

        // 2. 모든 유저
        List<SearchUserDTO> allUsersDTO = userRepository.findAll().stream()
                .map(user -> SearchUserDTO.builder()
                        .userId(user.getUserId())
                        .nickName(user.getNickname())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .joinedStudies(user.getJoinedStudies())
                        .wishStudies(user.getWishStudies())
                        .invitedStudies(user.getInvitedStudies())
                        .build()
                )
                .collect(Collectors.toList());
        System.out.println("All Users: " + allUsersDTO); // 디버깅
        return recommendUserService.recommendUsers(studyCondition, allUsersDTO);

    }
    //GPT: to

    // 스터디내 초대 현황 wishMembers & 내 수신함  invitedStudy 추가
    public void addWishMemberInvitedStudy(String studyId, String userId) {
        // 스터디조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 유저 조회
        TempUser tempUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 이미 신청요청 보낸 유저인지 확인
        Set<String> wishMember = (study.getWishMembers() == null) ? new HashSet<>() : study.getWishMembers();
        Set<String> invitedStudy = (tempUser.getInvitedStudies() == null) ? new HashSet<>() : tempUser.getInvitedStudies();
        if (wishMember.contains(userId) || invitedStudy.contains(studyId)) {
            throw new IllegalStateException("이미 가입 요청을 보낸 유저입니다."); // 임시: 500 에러
        }

        // 요청 목록에 추가
        study.getWishMembers().add(userId);
        tempUser.getInvitedStudies().add(studyId);

        // 저장
        studyRepository.save(study);
        userRepository.save(tempUser);

        sendToUser(tempUser.getFcmToken(), tempUser.getNickname(), study.getStudyName());

    }
    // 유저에게FCM 던지기
    public void sendToUser (String token,  String nickname,  String studyName ){
        String title = "유저에게 스터디 참가 요청";
        String body = studyName + "이 " + nickname + "을 스터디로 초대합니다!";
        System.out.println(token);

        firebaseMessagingService.sendNotification(token, title, body);
    }


//----------------------------------------------------------------------------------------------------------------------


//-------------------<< 스터디 추천 기능>>--------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<RecommendStudyDTO> getUserCondition(String userId) {
        // 1. 유저 조건  UserConditionDTO에 담기
        UserConditionDTO userCondition = userRepository.findById(userId)
                .map(user -> UserConditionDTO.builder()
                        .userId(user.getUserId())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .build())
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 2. 모든 스터디 StudyDTO에 담기
        List<StudyDTO> allStudiesDTO = studyRepository.findAll().stream()
                .map(study -> StudyDTO.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .build()
                )
                .collect(Collectors.toList());
        System.out.println("All Studies: " + allStudiesDTO); // 디버깅
        return recommendStudyService.recommendStudy(userCondition, allStudiesDTO);
    }


    //내 요청함 wishStudy & 스터디 내 수신함  preMembers 추가
    public void addWishStudyPreMember(String userId, String studyId) {
        // 유저 조회
        TempUser tempUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 이미 요청한 스터디인지 확인
        Set<String> preMembers = (study.getPreMembers() == null) ? new HashSet<>() : study.getPreMembers();
        Set<String> wishStudy = (tempUser.getWishStudies() == null) ? new HashSet<>() : tempUser.getWishStudies();
        if (wishStudy.contains(studyId) || preMembers.contains(userId)) {
            throw new IllegalStateException("이미 가입 요청을 보낸 스터디입니다."); // 임시: 500 에러
        }

        // 요청 목록에 추가
        tempUser.getWishStudies().add(studyId);
        study.getPreMembers().add(userId);

        // 저장
        userRepository.save(tempUser);
        studyRepository.save(study);

        // 스터디 팀장 정보
        String leaderId = study.getLeader(); // 스터디 장 ID 찾기
        TempUser leader = userRepository.findById(leaderId)  // 스터디 장의 Entity
                .orElseThrow(() -> new NoSuchElementException("유저ID" + leaderId + "에 해당하는 유저가 없습니다."));
        System.out.println("스터디장: " + leader);
        System.out.println("스터디장의 토큰: " + leader.getFcmToken());

        sendToLeader(leader.getFcmToken(), tempUser.getNickname(), study.getStudyName());
    }

    // 스터디 팀장에게 FCM 던지기
    public void sendToLeader (String token,  String nickname,  String studyName ){
        String title = "스터디에게 스터디 가입 신청";
        String body = nickname + "이(가) " + studyName + "에 가입하길 희망합니다!";
        System.out.println(token);

        firebaseMessagingService.sendNotification(token, title, body);
    }

//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  내 수신함  TempUser Service 로??? >>----------------------------------------------------------------
    // wishStudy 신청한 스터디 리스트: 나 -> 스터디
    @Transactional(readOnly = true)
    public List<MyWishStudyListDTO> myWishStudyList(String userId) {
        // 1. 내 정보 찾기
        TempUser tempUserInfo = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID " + userId + "에 해당하는 유저가 없습니다."));

        // 2. 내 wish 스터디들 Id 모으기 -> wishStudyEntities 만들기
        Set<String> studyIds = (tempUserInfo.getWishStudies() == null) ? new HashSet<>() : tempUserInfo.getWishStudies();
        List<Study> wishStudyEntities = studyRepository.findAllById(studyIds);

        // 3. wishStudyEntities 로 wishStudiesList 만들기
        List<StudyDTO> wishStudiesList = wishStudyEntities.stream()
                .map(study -> StudyDTO.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .members(study.getMembers())  // userID로 반환함 >>> nickname 필요하면 수정 필요
                        .count(study.getCount())
                        .build()
                )
                .collect(Collectors.toList());

        //4.  MyWishStudyListDTO 최종 반환
        return userRepository.findById(userId)
                .map(user -> MyWishStudyListDTO.builder()
                        .userId(user.getUserId())
                        .wishStudy(wishStudiesList)
                        .build()
                )
                .stream()
                .collect(Collectors.toList());

    }

    // invitedStudy 스카웃 요청 받은 스터디 (스터디 → 나)
    @Transactional(readOnly = true)
    public List<MyInvitedStudyListDTO> myInvitedStudyList(String userId) {
        // 1. 스터디 정보
        TempUser tempUserInfo = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID " + userId + "에 해당하는 유저가 없습니다."));

        // 2. 스터디ID 모으기 >>  invitedStudyEntities 에 넣기
        Set<String> studyIds = (tempUserInfo.getInvitedStudies() == null) ? new HashSet<>(): tempUserInfo.getInvitedStudies();
        List<Study> invitedStudyEntities = studyRepository.findAllById(studyIds);

        // 3. studyEntities 로 invitedStudiesList 만들기
        List<StudyDTO> invitedStudiesList = invitedStudyEntities.stream()
                .map(study -> StudyDTO.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers()) // userID로 반환함 >>> nickname 필요하면 수정 필요
                        .build()
                )
                .collect(Collectors.toList());


        return userRepository.findById(userId)
                .map(user -> MyInvitedStudyListDTO.builder()
                        .userId(user.getUserId())
                        .invitedStudy(invitedStudiesList)
                        .build()
                )
                .stream()
                .collect(Collectors.toList());

    }


//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  스터디 수신함   >>------------------------------------------------------------------------------

    // wishMembers 스카웃하고 싶은 스터디원 (내 스터디 → 사용자)
    @Transactional(readOnly = true)
    public List<StudyWishMembersListDTO> studyWishMembersList(String studyId) {

        // 1. 스터디 조회
        Study studyInfo = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2. 유저ID 모으기 >> wishUsersEntities 로 변환
        Set<String> userIds = (studyInfo.getWishMembers() == null) ? new HashSet<>() : studyInfo.getWishMembers();
        List<TempUser> wishUsersEntities = userRepository.findAllById(userIds);

        // 3. wishUsersEntities 를 UserDTO로 변환
        List<SearchUserDTO> wishUsersList = wishUsersEntities.stream()
                .map(tempUser -> SearchUserDTO.builder()
                        .userId(tempUser.getUserId())
                        .nickName(tempUser.getNickname())
                        .topics(tempUser.getTopics())
                        .meetingDays(tempUser.getMeetingDays())
                        .build()
                )
                .collect(Collectors.toList());

        // 4. 최종 StudyWishMembersListDTO 반혼
        return studyRepository.findById(studyId)
                .map(study -> StudyWishMembersListDTO.builder()
                        .studyId(study.getId())
                        .wishMembers(wishUsersList)
                        .build()
                )
                .stream()
                .collect(Collectors.toList());
    }

    // preMembers 신청한 스터디원 (사용자→ 내 스터디)
    @Transactional(readOnly = true)
    public List<StudyPreMembersListDTO> studyPreMembersList(String studyId) {
        // 1. 스터디 조회
        Study studyInfo = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2. 유저ID 모으기 >> preUsersEntities 로 변환
        Set<String> userIds = (studyInfo.getPreMembers() == null) ? new HashSet<>() : studyInfo.getPreMembers();
        List<TempUser> preUsersEntities = userRepository.findAllById(userIds);

        // 3. preUsersEntities 를 UserDTO로 변환
        List<SearchUserDTO> preUsersList = preUsersEntities.stream()
                .map(tempUser -> SearchUserDTO.builder()
                        .userId(tempUser.getUserId())
                        .nickName(tempUser.getNickname())
                        .topics(tempUser.getTopics())
                        .meetingDays(tempUser.getMeetingDays())
                        .build()
                )
                .collect(Collectors.toList());

        // 4. 최종 StudyWishMembersListDTO 반혼
        return studyRepository.findById(studyId)
                .map(study -> StudyPreMembersListDTO.builder()
                        .studyId(study.getId())
                        .preMembers(preUsersList)
                        .build()
                )
                .stream()
                .collect(Collectors.toList());
    }


//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  요청 수락 >>------------------------------------------------------------------------------
    // joinedStudy&member 추가
    public void addJoinedStudyMember(String userId, String studyId) {
        // 1. 유저&스터디 확인
        TempUser tempUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2. 이미 가입된 스터디인지 & 이미 가입된 유저인지 확인
        Set<String> joinedStudies = (tempUser.getJoinedStudies() == null) ? new HashSet<>() : tempUser.getJoinedStudies();

        if (joinedStudies.contains(studyId) || study.getMembers().contains(userId)) {
            throw new IllegalStateException("이미 가입한 스터디입니다."); // 임시: 500 에러
        }

        // 3. 추가
        tempUser.getJoinedStudies().add(studyId);
        study.getMembers().add(userId);

        // 4. 저장
        userRepository.save(tempUser);
        studyRepository.save(study);


        // 스터디 팀장 정보
        String leaderId = study.getLeader(); // 스터디 장 ID 찾기
        TempUser leader = userRepository.findById(leaderId)  // 스터디 장의 Entity
                .orElseThrow(() -> new NoSuchElementException("유저ID" + leaderId + "에 해당하는 유저가 없습니다."));

        sendToLeaderJoinEvent(leader.getFcmToken(), tempUser.getNickname(), study.getStudyName());

    }

    // 스터디 팀장에게 FCM 던지기
    public void sendToLeaderJoinEvent (String token,  String nickname,  String studyName ){
        String title = "유저의 스터디 가입";
        String body = nickname + "이(가) " + studyName + "에 가입하셨습니다!";

        firebaseMessagingService.sendNotification(token, title, body);
    }



    // 유저의 수락: invitedStudy, wishMembers 에서 studyId, userId 삭제
    public void editInvitedStudyWishMembers(String userId, String studyId) {
        TempUser tempUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        tempUser.getInvitedStudies().remove(studyId);
        study.getWishMembers().remove(userId);

        userRepository.save(tempUser);
        studyRepository.save(study);

    }

    // 스터디의 수락: wishStudy, preMembers 수정
    public void editWishStudyPreMembers(String userId, String studyId) {
        TempUser tempUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        tempUser.getWishStudies().remove(studyId);
        study.getPreMembers().remove(userId);

        userRepository.save(tempUser);
        userRepository.save(tempUser);
        studyRepository.save(study);

    }

//----------------------------------------------------------------------------------------------------------------------


    // GPT: from
    // 스터디 수정
    public void updateStudy(String id, StudyUpdateRequestDTO studyUpdateRequestDTO) {
        // 1. 기존 스터디 조회
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("스터디 ID " + id + "에 해당하는 스터디가 존재하지 않습니다."));

        //2. DTO로 전달된 값으로 스터디 필드 수정
        if (studyUpdateRequestDTO.getStudyName() != null) {
            study.setStudyName(studyUpdateRequestDTO.getStudyName());
        }

        if (studyUpdateRequestDTO.getTopic() != null) {
            study.setTopic(studyUpdateRequestDTO.getTopic());
        }

        if (studyUpdateRequestDTO.getMeetingDays() != null) {
            study.setMeetingDays(studyUpdateRequestDTO.getMeetingDays());
        }

        if (studyUpdateRequestDTO.getCount() > 0) {
            study.setCount(studyUpdateRequestDTO.getCount());
        }

        if (studyUpdateRequestDTO.getStudyContent() != null) {
            study.setStudyContent(studyUpdateRequestDTO.getStudyContent());
        }

        // 3. 수정된 스터디 저장
        studyRepository.save(study);

    }
    // GPT: to



    // 스터디 삭제
    public void deleteStudy(String id) {
        studyRepository.deleteById(id);
    }

}
