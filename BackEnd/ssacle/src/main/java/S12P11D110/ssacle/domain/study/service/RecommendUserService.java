package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.domain.study.dto.RecommendUserDTO;
import S12P11D110.ssacle.domain.study.dto.StudyConditionDTO;
import S12P11D110.ssacle.domain.study.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// GPT: ALL
@Service
public class RecommendUserService {

    public List<RecommendUserDTO> recommendUsers(StudyConditionDTO studyCondition, List<UserDTO> allUsers){

        // 1. 유저 필터링
        List<UserDTO> filteredUsers = allUsers.stream()
                .filter(user-> user.getTopic()!=null && user.getMeetingDay()!=null) // topic과 meetingDay가 등록된 유저
                .filter(user -> {
                    // 가입된 스터디가 없는 유저는 Collections.emptyList()을 반환(null 방지)
                    Set<String> joinedStudies = user.getJoinedStudies() != null ? user.getJoinedStudies() : Collections.emptySet();
                    return !joinedStudies.contains(studyCondition.getId());
                        }) // 해당 스터디에 가입된 유저는 제외
                .collect(Collectors.toList());

        // 2. 코사인 유사도를 기반으로 유저 추천
        Map<UserDTO, Double> userSimilarityMap = new HashMap<>();
        for(UserDTO user : filteredUsers){
            double similarity = calculateCosineSimilarity(studyCondition, user);
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
                        entry.getKey().getTopic(), // Topic
                        entry.getKey().getMeetingDay() // MeetingDay
                )) // DTO 변환
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarity(StudyConditionDTO studyCondition, UserDTO user) {
        // 1. 스터디와 유저의 주제 및 모임 요일 벡터화
        Set<String> studyFeatures = new HashSet<>();
        studyFeatures.addAll(studyCondition.getTopic().stream().map(Enum::name).collect(Collectors.toList())); // Enum → String 변환
        studyFeatures.addAll(studyCondition.getMeetingDay().stream().map(Enum::name).collect(Collectors.toList())); // Enum → String 변환

        Set<String> userFeatures = new HashSet<>();
        user.getTopic().forEach(topic -> userFeatures.add(topic.name())); // Enum → String 변환
        user.getMeetingDay().forEach(day -> userFeatures.add(day.name())); // Enum → String 변환

        // 2. 교집합 및 합집합 크기를 계산
        Set<String> intersection = new HashSet<>(studyFeatures);
        intersection.retainAll(userFeatures); // 교집합

        Set<String> union = new HashSet<>(studyFeatures);
        union.addAll(userFeatures); // 합집합

        // 3. 코사인 유사도 계산
        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / Math.sqrt(studyFeatures.size() * userFeatures.size());
    }

}
