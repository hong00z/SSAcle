package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.domain.study.dto.RecommendUserDTO;
import S12P11D110.ssacle.domain.study.dto.StudyConditionDTO;
import S12P11D110.ssacle.domain.study.dto.StudyDTO;
import S12P11D110.ssacle.domain.study.dto.UserConditionDTO;
import S12P11D110.ssacle.domain.tempUser.SearchUserDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// GPT: ALL
@Service
public class RecommendUserService {

    public List<RecommendUserDTO> recommendUsers(StudyConditionDTO studyCondition, List<SearchUserDTO> allUsers){

        String studyTopic = studyCondition.getTopic();
        // 1. 유저 필터링
        List<SearchUserDTO> filteredUsers = allUsers.stream()
                .filter(user-> user.getTopics()!=null && user.getMeetingDays()!=null) // topic과 meetingDay가 등록된 유저
                .filter(user-> user.getTopics().contains(studyTopic)) // 스터디 주제에 해당 안되는 유저는 제외
                .filter(user -> {
                    // 가입된 스터디가 없는 유저는 Collections.emptyList()을 반환(null 방지)
                    Set<String> joinedStudies = user.getJoinedStudies() != null ? user.getJoinedStudies() : Collections.emptySet();
                    Set<String> wishStudies = user.getWishStudies() != null ? user.getWishStudies() : Collections.emptySet();
                    Set<String> invitedStudies = user.getInvitedStudies() != null ? user.getInvitedStudies() : Collections.emptySet();

                    boolean isAlreadyJoined = joinedStudies.contains(studyCondition.getId());
                    boolean isAlreadyWish = wishStudies.contains(studyCondition.getId());
                    boolean isAlreadyInvited = invitedStudies.contains(studyCondition.getId());

                    // 가입 여부, 초대 여부 확인
                    System.out.println("유저 Id: "+user.getUserId()+", 이미 가입된 유저인가?"+isAlreadyJoined);
                    System.out.println("유저 Id: "+user.getUserId()+", 이미 요청한 유저인가?"+isAlreadyWish);
                    System.out.println("유저 Id: "+user.getUserId()+", 이미 요청된 유저인가?"+isAlreadyInvited);

                    return !(isAlreadyJoined ||isAlreadyWish||isAlreadyInvited);
                }) // 해당 스터디에 가입된 유저 Or 이미 스터디에 가입 요청 받은 유저 or 이미 요청온 유저 제외
                .toList();
        System.out.println("스터디의 topic 원본 데이터: " + studyCondition.getTopic());
        System.out.println("스터디의 topic 실제 타입: " + studyCondition.getTopic().getClass().getName());

        System.out.println("✅ 필터링 후 남은 유저 리스트: " + filteredUsers);

        // 2. 코사인 유사도를 기반으로 유저 추천
        Map<SearchUserDTO, Double> userSimilarityMap = new HashMap<>();
        for(SearchUserDTO user : filteredUsers){
            double similarity = calculateFiltering(studyCondition, user);
            userSimilarityMap.put(user, similarity);

            System.out.println("Calculating similarity for user: " + user.getUserId()); // 디버깅
            System.out.println("Similarity: " + similarity); // 디버깅
        }

        System.out.println("User Similarity Map: " + userSimilarityMap); // 디버깅

        // 3. 유사도 순으로 내림차순 정렬 -> 상위 3명 유저 추출
        return userSimilarityMap.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue())) // 유사도 내림차순
                .limit(3) // 상위 3명 추출
                .map(entry -> new RecommendUserDTO(
                        entry.getKey().getUserId(), // User ID
                        entry.getValue(), // 유사도 점수
                        entry.getKey().getNickName(), // Nickname
                        entry.getKey().getTopics(),   // Topic
                        entry.getKey().getMeetingDays() // MeetingDay
                )) // DTO 변환
                .collect(Collectors.toList());
    }

    private double calculateFiltering(StudyConditionDTO studyCondition, SearchUserDTO user) {
        // 1. 스터디와 유저의 주제 및 모임 요일 벡터화
        Set<String> studyFeatures = new HashSet<>();
        studyFeatures.add(studyCondition.getTopic());
        studyFeatures.addAll(studyCondition.getMeetingDays());

        Set<String> userFeatures = new HashSet<>();
        userFeatures.addAll(user.getTopics());
        userFeatures.addAll(user.getMeetingDays());


        // 2. 교집합 및 합집합 크기를 계산
        Set<String> intersection = new HashSet<>(studyFeatures);
        intersection.retainAll(userFeatures); // 교집합

        Set<String> union = new HashSet<>(studyFeatures);
        union.addAll(userFeatures); // 합집합

        // 3. 코사인 유사도 계산
        if (union.isEmpty()) return 0.0;
        double cosineResult = intersection.size() / Math.sqrt(studyFeatures.size() * userFeatures.size());


        double filterResult = calculateFilterResult(studyCondition, user);
        return (cosineResult + filterResult) / 2;
    }


    private double calculateFilterResult(StudyConditionDTO studyCondition, SearchUserDTO user) {


        // 1. study의topic 개수, meetingDay 개수
        int topicCount = 1;
        int meetingDaysCount = studyCondition.getMeetingDays().size();

        // 2. eachOfTopic / eachOfMeetingDay 값 구하기
        double eachOfTopic = 0.5 / topicCount;
        double eachOfMeetingDays = 0.5 / meetingDaysCount;

        // 3.겹치는 topic 안겹치는 topic 겹치는 meetingday 안겹치는 meetingday 개수 구하기
        Set<String> studyTopic = new HashSet<>(Set.of(studyCondition.getTopic()));
        Set<String> userTopic = new HashSet<>(user.getTopics());
//        user.getTopics().forEach(topics -> userTopic.add(topics.name()));
        Set<String> studyMeetingDays = new HashSet<>(studyCondition.getMeetingDays());
        Set<String> userMeetingDays = new HashSet<>(user.getMeetingDays());
//        user.getMeetingDays().forEach(meetingDays -> userMeetingDays.add(meetingDays.name()));
        // topic의 교집합 구하기
        Set<String> topicIntersection = new HashSet<>(studyTopic);
        topicIntersection.retainAll(userTopic);
        //meetingDay의 교집합 구하기
        Set<String> meetingDayIntersection = new HashSet<>(studyMeetingDays);
        meetingDayIntersection.retainAll(userMeetingDays);

        int sameTopics = topicIntersection.size();
        int diffTopics = topicCount - sameTopics;
        int sameMeetingDays = meetingDayIntersection.size();
        int diffMeetingDays = meetingDaysCount - sameMeetingDays;

        // 4. 점수 더하기고 평균 내기
        return  (topicCount * eachOfTopic - (diffTopics * eachOfTopic * 0.001))
                + (meetingDaysCount * eachOfMeetingDays - (diffMeetingDays * 0.001));


    }
}
