package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.SsacleApplication;
import S12P11D110.ssacle.domain.feed.dto.FeedCreatorInfo;
import S12P11D110.ssacle.domain.feed.dto.FeedDetailDTO;
import S12P11D110.ssacle.domain.feed.entity.Feed;
import S12P11D110.ssacle.domain.feed.repository.FeedRepository;
import S12P11D110.ssacle.domain.study.dto.*;
import S12P11D110.ssacle.domain.study.dto.request.StudyCreateRequest;
import S12P11D110.ssacle.domain.study.dto.request.StudyUpdateRequest;
import S12P11D110.ssacle.domain.study.dto.response.*;
import S12P11D110.ssacle.domain.study.repository.StudyRepository;
import S12P11D110.ssacle.domain.study.dto.SearchUser;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
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
//    private final TempUserRepository userRepository;
    private final UserRepository userRepository;
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
    public void saveStudy(String userId, StudyCreateRequest studyCreateRequest) {
        S12P11D110.ssacle.domain.study.entity.Study study = new S12P11D110.ssacle.domain.study.entity.Study();
        study.setStudyName(studyCreateRequest.getStudyName());
        study.setTopic(studyCreateRequest.getTopic());
        study.setMeetingDays(studyCreateRequest.getMeetingDays());
        study.setCount(studyCreateRequest.getCount());
        study.setStudyContent(studyCreateRequest.getStudyContent());

        // members: 현재 스터디 개설자의 userId가 들어가야함
        Set<String> members = new HashSet<>();
        members.add(userId);
        study.setMembers(members);

        // WishMembers, PreMember는 개설 단계에서 입력하지 않음
        study.setWishMembers(new HashSet<>());
        study.setPreMembers(new HashSet<>());

        studyRepository.save(study);

        // 유저의 joinedStudies에 스터디 아이디 추가
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NoSuchElementException("유저ID " + userId + "에 해당하는 유저가 없습니다."));

        Set<String> joinedStudies = (user.getJoinedStudies() == null) ? new HashSet<>() : user.getJoinedStudies();
        joinedStudies.add(study.getId());
        user.setJoinedStudies(joinedStudies);

        // 유저의 createdStudies 에 스터디 아이디 추가
        Set<String> createdStudies = user.getCreatedStudies();
        createdStudies.add(study.getId());
        user.setCreatedStudies(createdStudies);

        userRepository.save(user);

    }

    //gpt: from
    // 모든 스터디 조회
    @Transactional(readOnly = true)
    public List<StudyResponse> getAllStudy() {
        //1. repository에서 엔티티 가져오기
        List<S12P11D110.ssacle.domain.study.entity.Study> studies = studyRepository.findAll();

        //2. stream api를 사용해서 DTO로 변환
        return studies.stream()
                .map(study -> StudyResponse.builder()
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
    public List<StudyResponse> getStudiesByConditions(Set<String> topics, Set<String> meetingDays) {
        List<S12P11D110.ssacle.domain.study.entity.Study> studies;

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
                .map(study -> StudyResponse.builder()
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
    public StudyDetail getStudyById(String studyId) {
        // 1. 해당 스터디 찾기, 스터디에 해당하는 피드들 찾기
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID " + studyId + "에 해당하는 스터디가 없습니다."));

        System.out.println("현재 스터디 " + study);
        //2. 스터디에 해당 하는 feed, member 찾기
        // 스터디에 해당하는 feed 모으기
        List<Feed> feedEntities= feedRepository.findByStudy(studyId);
        if(feedEntities.isEmpty()){
            feedEntities = new ArrayList<>() {
            };
        }

        // 스터디 멤버들 Id 모으기, 실제 User 엔티티 조회
        Set<String> userIds = study.getMembers();
        List<User> UserEntities = userRepository.findAllById(userIds);
        System.out.println("스터디 멤버들 " + UserEntities);


        // 3. 조회된 피드, 유저를 feedList, nikcknameList 변환
        // gpt: from ---------------------------------------------------------
        List<FeedDetailDTO> feedList = feedEntities.stream()
                .map(feed ->{
                    // 작성자의 ID 가져오기]
                    String userId = feed.getCreatedBy();
                    //user 조회
                    User user = userRepository.findById(userId)
                            .orElseThrow(()-> new NoSuchElementException("유저ID " +userId+ " 에 해당하는 유저가 없습니다." ));

                    // FeedCreatorInfo: 유저의 아이디, 닉네임, 이미지, 텀 , 캠퍼스
                    FeedCreatorInfo creatorInfo = FeedCreatorInfo.builder()
                            .userId(user.getUserId())
                            .nickname(user.getNickname())
                            .image(user.getImage())
                            .term(user.getTerm())
                            .campus(user.getCampus())
                            .build();

                    //DTO 변환
                    return FeedDetailDTO.builder()
                            .study(feed.getStudy())
                            .creatorInfo(creatorInfo)
                            .title(feed.getTitle())
                            .content(feed.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        System.out.println("스터디 피드 " + feedList);
        // gpt: to ---------------------------------------------------------

        // 스터디 가입 멤버 이름 리스트
        List<nicknameImage> memberInfo = UserEntities.stream()
                .map(user -> nicknameImage.builder()
                        .nickname(user.getNickname())
                        .image(user.getImage())
                        .isCreator(study.getCreatedBy().equals(user.getUserId()))
                        .build()
                ).toList();

        System.out.println("멤버 정보 상세" + memberInfo);

        // 4. StudyDetailDTO 반환
        return StudyDetail.builder()
                .id(study.getId())
                .studyName(study.getStudyName())
                .topic(study.getTopic())
                .meetingDays(study.getMeetingDays())
                .count(study.getCount())
                .members(memberInfo)
                .memberCont(study.getMembers().size())
                .studyContent(study.getStudyContent())
                .feeds(feedList)
                .createdBy(study.getCreatedBy())
                .build();

    }
    // gpt:to

    // 내가 참여중인 스터디 리스트
    @Transactional(readOnly = true)
    public List<MyStudyList> getStudiesByUserId(String userId) {

        // 멤버이름, 멤버의 프로필 이미지  DTO
        // 내가 참여 중인 스터디
        List<S12P11D110.ssacle.domain.study.entity.Study> studies = studyRepository.findByMembersContaining(userId);




        // MyStudyList 반환
        return studies.stream()
                .map(study ->{
                    // 해당 스터디의 멤버 ID 리스트 가져오기
                    List<String> membersId = new ArrayList<>(study.getMembers());
                    System.out.println("스터디ID: " + study.getId());
                    System.out.println("스터디 개설한 사람: " + study.getCreatedBy());

                    // 멤버 ID를 이용해 유저 정보 조회 (닉네임, 이미지)
                    List<nicknameImage> memberInfo = userRepository.findAllById(membersId).stream()
                            .map(user-> new nicknameImage(
                                    user.getNickname(),
                                    user.getImage(),
                                    study.getCreatedBy().equals(user.getUserId())
                            ))
                            .collect(Collectors.toList());


                    return MyStudyList.builder()
                            .id(study.getId())
                            .studyName(study.getStudyName())
                            .topic(study.getTopic())
                            .meetingDays(study.getMeetingDays())
                            .count(study.getCount())
                            .memberCount(study.getMembers().size())
                            .members(memberInfo)
                            .studyContent(study.getStudyContent())
                            .createdBy(study.getCreatedBy())
                            .build();
                })
                .collect(Collectors.toList());
    }

    //-------------------<< 스터디원 추천 기능>>-------------------------------------------------------------------------------
    //GPT: from
    // 스터디 주제, 모임 요일 정보 요약
    @Transactional(readOnly = true)
    public List<RecommendUser> getStudyCondition(String studyId) {
        // 1. 스터디 조건
        StudyCondition studyCondition = studyRepository.findById(studyId)
                .map(study -> StudyCondition.builder()
                        .id(study.getId())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .build())
                .orElseThrow(() -> new NoSuchElementException("스터디 ID " + studyId + "에 해당하는 스터디가 존재하지 않습니다."));

        System.out.println("Study Condition: " + studyCondition); // 디버깅

        // 2. 모든 유저
        List<SearchUser> allUsersDTO = userRepository.findAll().stream()
                .map(user -> SearchUser.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .image(user.getImage())
                        .term(user.getTerm())
                        .campus(user.getCampus())
                        .countJoinedStudies(user.getJoinedStudies().size())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .joinedStudies(user.getJoinedStudies())
                        .wishStudies(user.getWishStudies())
                        .invitedStudies(user.getInvitedStudies())
                        .build()
                )
                .collect(Collectors.toList());
        System.out.println("All Users: " + allUsersDTO); // 디버깅
        return recommendUserService.recommendUsers(studyCondition, allUsersDTO).stream()
                .map(user-> RecommendUser.builder()
                                .userId(user.getUserId())
                                .similarity(user.getSimilarity())
                                .nickname(user.getNickname())
                                .image(user.getImage())
                                .term(user.getTerm())
                                .campus(user.getCampus())
                                .countJoinedStudies(user.getCountJoinedStudies())
                                .topics(user.getTopics())
                                .meetingDays(user.getMeetingDays())
                                .build()
                ).collect(Collectors.toList());

    }
    //GPT: to

    // 스터디내 초대 현황 wishMembers & 내 수신함  invitedStudy 추가
    public void addWishMemberInvitedStudy(String studyId, String userId) {
        // 스터디조회
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 이미 신청요청 보낸 유저인지 확인
        Set<String> wishMember = (study.getWishMembers() == null) ? new HashSet<>() : study.getWishMembers();
        Set<String> invitedStudy = (user.getInvitedStudies() == null) ? new HashSet<>() : user.getInvitedStudies();
        if (wishMember.contains(userId) || invitedStudy.contains(studyId)) {
            throw new IllegalStateException("이미 가입 요청을 보낸 유저입니다."); // 임시: 500 에러
        }

        // 요청 목록에 추가
        study.getWishMembers().add(userId);
        user.getInvitedStudies().add(studyId);

        // 저장
        studyRepository.save(study);
        userRepository.save(user);

        sendToUser(user.getFcmToken(), user.getNickname(), study.getStudyName());

    }
    // 유저에게 FCM 던지기
    public void sendToUser (String token,  String nickname,  String studyName ){
        String title = "유저에게 스터디 참가 요청";
        String body = studyName + "이 " + nickname + "을 스터디로 초대합니다!";
        System.out.println(token);

        firebaseMessagingService.sendNotification(token, title, body);
    }


//----------------------------------------------------------------------------------------------------------------------


//-------------------<< 스터디 추천 기능>>--------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<RecommendStudy> getUserCondition(String userId) {
        // 1. 유저 조건  UserConditionDTO에 담기
        UserCondition userCondition = userRepository.findById(userId)
                .map(user -> UserCondition.builder()
                        .userId(user.getUserId())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .build())
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 2. 모든 스터디 StudyDTO에 담기
        List<Study> allStudiesDTO = studyRepository.findAll().stream()
                .map(study -> Study.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .createdBy(study.getCreatedBy())
                        .build()
                )
                .collect(Collectors.toList());
        System.out.println("All Studies: " + allStudiesDTO); // 디버깅

        return recommendStudyService.recommendStudy(userCondition, allStudiesDTO).stream()
                .map(study-> RecommendStudy.builder()
                        .studyId(study.getStudyId())
                        .similarity(study.getSimilarity())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .memberCount(study.getMembers().size())
                        .members(study.getMembers().stream()  // 멤버의 닉네임과 이미지 방장정보 넣음
                                .map(user -> {
                                    String creator = study.getCreatedBy();
                                    User userEntity = userRepository.findById(user)
                                            .orElseThrow(()->new NoSuchElementException("유저ID" + userId + "를 찾을 수 없습니다."));
                                    return nicknameImage.builder()
                                                    .nickname(userEntity.getNickname())
                                                    .image(userEntity.getImage())
                                                    .isCreator(userEntity.getUserId().equals(creator))
                                                    .build();
                                }).collect(Collectors.toList())
                        ).build()


                ).collect(Collectors.toList());
    }


    //내 요청함 wishStudy & 스터디 내 수신함  preMembers 추가
    public void addWishStudyPreMember(String userId, String studyId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));

        // 스터디 조회
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 이미 요청한 스터디인지 확인
        Set<String> preMembers = (study.getPreMembers() == null) ? new HashSet<>() : study.getPreMembers();
        Set<String> wishStudy = (user.getWishStudies() == null) ? new HashSet<>() : user.getWishStudies();
        if (wishStudy.contains(studyId) || preMembers.contains(userId)) {
            throw new IllegalStateException("이미 가입 요청을 보낸 스터디입니다."); // 임시: 500 에러
        }

        // 요청 목록에 추가
        user.getWishStudies().add(studyId);
        study.getPreMembers().add(userId);

        // 저장
        userRepository.save(user);
        studyRepository.save(study);

        // 스터디 팀장 정보
        String leaderId = study.getCreatedBy(); // 스터디 장 ID 찾기
        User leader = userRepository.findById(leaderId)  // 스터디 장의 Entity
                .orElseThrow(() -> new NoSuchElementException("유저ID" + leaderId + "에 해당하는 유저가 없습니다."));
        System.out.println("스터디장: " + leader);
        System.out.println("스터디장의 토큰: " + leader.getFcmToken());

        sendToLeader(leader.getFcmToken(), user.getNickname(), study.getStudyName());
    }

    // 스터디 팀장에게 FCM 던지기
    public void sendToLeader (String token,  String nickname,  String studyName ){
        String title = "스터디에게 스터디 가입 신청";
        String body = nickname + "이(가) " + studyName + "에 가입하길 희망합니다!";
        System.out.println(token);

        firebaseMessagingService.sendNotification(token, title, body);
    }

//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  내 수신함  user Service 로??? >>----------------------------------------------------------------
    // wishStudy 신청한 스터디 리스트: 나 -> 스터디
    @Transactional(readOnly = true)
    public List<WishInvitedStudies> myWishStudyList(String userId) {
        // 1. 내 정보 찾기
        User userInfo = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID " + userId + "에 해당하는 유저가 없습니다."));

        // 2. InvitedStudies ID 목록 (만약 null이면 빈 Set을 반환)
        Set<String> studyIds = Optional.ofNullable(userInfo.getWishStudies())
                .orElse(Collections.emptySet());

        // WishInvitedStudies로 반환
        return studyRepository.findAllById(studyIds).stream()
                .map(study-> WishInvitedStudies.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .build()
                ).collect(Collectors.toList());

    }

    // invitedStudy 스카웃 요청 받은 스터디 (스터디 → 나)
    @Transactional(readOnly = true)
    public List<WishInvitedStudies> myInvitedStudyList(String userId) {
        // 1. 스터디 정보
        User userInfo = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID " + userId + "에 해당하는 유저가 없습니다."));

        // 2. InvitedStudies ID 목록 (만약 null이면 빈 Set을 반환)
        Set<String> studyIds = Optional.ofNullable(userInfo.getInvitedStudies())
                .orElse(Collections.emptySet());

        // WishInvitedStudies로 반환
        return studyRepository.findAllById(studyIds).stream()
                .map(study-> WishInvitedStudies.builder()
                        .studyId(study.getId())
                        .studyName(study.getStudyName())
                        .topic(study.getTopic())
                        .meetingDays(study.getMeetingDays())
                        .count(study.getCount())
                        .members(study.getMembers())
                        .build()
                ).collect(Collectors.toList());

    }


//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  스터디 수신함   >>------------------------------------------------------------------------------

    // wishMembers 스카웃하고 싶은 스터디원 (내 스터디 → 사용자)
    @Transactional(readOnly = true)
    public List<WishPreMembers> studyWishMembersList(String studyId) {

        // 1. 스터디 조회
        S12P11D110.ssacle.domain.study.entity.Study studyInfo = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2. Wishmember ID 목록 (만약 null이면 빈 Set을 반환)
        Set<String> userIds = Optional.ofNullable(studyInfo.getWishMembers())
                .orElse(Collections.emptySet());

        // 3. WishPreMembers DTO 로 반환
        return userRepository.findAllById(userIds).stream()
                .map(user -> WishPreMembers.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .image(user.getImage())
                        .term(user.getTerm())
                        .campus(user.getCampus())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // preMembers 신청한 스터디원 (사용자→ 내 스터디)
    @Transactional(readOnly = true)
    public List<WishPreMembers> studyPreMembersList(String studyId) {
        // 1. 스터디 조회
        S12P11D110.ssacle.domain.study.entity.Study studyInfo = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2.  PreMembers ID 목록 (만약 null이면 빈 Set을 반환)
        Set<String> userIds = Optional.ofNullable(studyInfo.getPreMembers())
                .orElse(Collections.emptySet());

        // 3. WishPreMembers로 변환
        return userRepository.findAllById(userIds).stream()
                .map(user-> WishPreMembers.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .image(user.getImage())
                        .term(user.getTerm())
                        .campus(user.getCampus())
                        .topics(user.getTopics())
                        .meetingDays(user.getMeetingDays())
                        .build()
                ).collect(Collectors.toList());

    }


//----------------------------------------------------------------------------------------------------------------------


//--------------------<<  요청 수락 >>------------------------------------------------------------------------------
    // joinedStudy&member 추가
    public void addJoinedStudyMember(String userId, String studyId) {
        // 1. 유저&스터디 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        // 2. 이미 가입된 스터디인지 & 이미 가입된 유저인지 확인
        Set<String> joinedStudies = (user.getJoinedStudies() == null) ? new HashSet<>() : user.getJoinedStudies();

        if (joinedStudies.contains(studyId) || study.getMembers().contains(userId)) {
            throw new IllegalStateException("이미 가입한 스터디입니다."); // 임시: 500 에러
        }

        // 3. 추가
        user.getJoinedStudies().add(studyId);
        study.getMembers().add(userId);

        // 4. 저장
        userRepository.save(user);
        studyRepository.save(study);


        // 스터디 팀장 정보
        String leaderId = study.getCreatedBy(); // 스터디 장 ID 찾기
        User leader = userRepository.findById(leaderId)  // 스터디 장의 Entity
                .orElseThrow(() -> new NoSuchElementException("유저ID" + leaderId + "에 해당하는 유저가 없습니다."));

        sendToLeaderJoinEvent(leader.getFcmToken(), user.getNickname(), study.getStudyName());

    }

    // 스터디 팀장에게 FCM 던지기
    public void sendToLeaderJoinEvent (String token,  String nickname,  String studyName ){
        String title = "유저의 스터디 가입";
        String body = nickname + "이(가) " + studyName + "에 가입하셨습니다!";

        firebaseMessagingService.sendNotification(token, title, body);
    }



    // 유저의 수락: invitedStudy, wishMembers 에서 studyId, userId 삭제
    public void editInvitedStudyWishMembers(String userId, String studyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        user.getInvitedStudies().remove(studyId);
        study.getWishMembers().remove(userId);

        userRepository.save(user);
        studyRepository.save(study);

    }

    // 스터디의 수락: wishStudy, preMembers 수정
    public void editWishStudyPreMembers(String userId, String studyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저ID" + userId + "에 해당하는 유저가 없습니다."));
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));

        user.getWishStudies().remove(studyId);
        study.getPreMembers().remove(userId);

        userRepository.save(user);
        userRepository.save(user);
        studyRepository.save(study);

    }

//----------------------------------------------------------------------------------------------------------------------


    // GPT: from
    // 스터디 수정
    public void updateStudy(String id, StudyUpdateRequest studyUpdateRequestDTO) {
        // 1. 기존 스터디 조회
        S12P11D110.ssacle.domain.study.entity.Study study = studyRepository.findById(id)
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
