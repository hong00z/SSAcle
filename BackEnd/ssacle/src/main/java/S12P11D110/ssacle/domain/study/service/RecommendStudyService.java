package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.domain.study.dto.RecommendStudyDTO;
import S12P11D110.ssacle.domain.study.dto.StudyDTO;
import S12P11D110.ssacle.domain.study.dto.UserConditionDTO;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendStudyService {
    public List<RecommendStudyDTO> recommendStudy(UserConditionDTO userCondition, List<StudyDTO>allStudiesDTO){
        Set<String> userTopics = userCondition.getTopics().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        //1. 스터디 필터링
        List<StudyDTO> filteredStudies = allStudiesDTO.stream()
                .filter(study -> study.getCount()  > study.getMembers().size() ) // 정원 > 가입멤버수인 스터디
                .filter(study -> study.getTopic().stream().map(Enum::name).anyMatch(userTopics::contains)) //anyMatch(Predicate<T>) → 스트림에서 하나라도 조건을 만족하면 true 반환.
                .filter(study -> { // member, wishMember preMember는 제외
                    // member, wishMember preMember객체 가져오기
                    Set<String> members = study.getMembers() != null ? study.getMembers() : new HashSet<>();
                    Set<String> wishMembers = study.getWishMembers() != null ? study.getWishMembers() : new HashSet<>();
                    Set<String> preMembers = study.getPreMembers() != null ? study.getPreMembers() : new HashSet<>();

                    boolean isAlreadyMembers = members.contains(userCondition.getUserId().toString());
                    boolean isAlreadyWishMembers = wishMembers.contains(userCondition.getUserId().toString());
                    boolean isAlreadyPreMembers = preMembers.contains(userCondition.getUserId().toString());


                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 가입한 스터디인가?"+ isAlreadyMembers);
                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 요청받은 스터디인가?"+ isAlreadyWishMembers);
                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 가입 신청한 스터디인가?"+ isAlreadyPreMembers);

                    return !(isAlreadyMembers||isAlreadyWishMembers||isAlreadyPreMembers);
                })
                .collect(Collectors.toList());

        System.out.println(filteredStudies);

        // 2. 코사인 유사도를 기반으로 스터디 추천

        Map<StudyDTO, Double> studySimilarityMap = new HashMap<>();
        for(StudyDTO study : filteredStudies){
            double similarity = calculateCosineSimilarity(userCondition, study);
            studySimilarityMap.put(study, similarity);

            System.out.println(study + ": " + similarity); // 디버깅
        }
        System.out.println("User Similarity Map: " + studySimilarityMap); // 디버깅

        //3. 유사도 순으로 내림차순 -> 상위 3개 스터디 추출
        return studySimilarityMap.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(3)
                .map(entry -> new RecommendStudyDTO(
                        entry.getKey().getStudyId(),
                        entry.getValue(), // 유사도
                        entry.getKey().getStudyName(),
                        entry.getKey().getTopic(),
                        entry.getKey().getMeetingDays()
                ))
                .collect(Collectors.toList());
    }

    // 코사인 유사도 계산
    public double calculateCosineSimilarity(UserConditionDTO userCondition , StudyDTO study){
        // 1. 유저와 스터디의 주제 및 모임 요일 백터화
        Set<String> userFeatures = new HashSet<>();
        userFeatures.addAll(userCondition.getTopics().stream().map(Enum::name).collect(Collectors.toList()));
        userFeatures.addAll(userCondition.getMeetingDays().stream().map(Enum::name).collect(Collectors.toList()));

        Set<String> studyFeatures = new HashSet<>();
        study.getTopic().forEach(topic -> studyFeatures.add(topic.name()));
        study.getMeetingDays().forEach(day -> studyFeatures.add(day.name()));

        // 2. 교집합 및 합집합 크기를 계산
        Set<String> intersection = new HashSet<>(userFeatures);
        intersection.retainAll(studyFeatures); // 교집합

        Set<String> union = new HashSet<>(userFeatures);
        union.addAll(studyFeatures); // 합집합

        // 3. 코사인 유사도 계산
        if(union.isEmpty()) return 0.0;
        return (double) intersection.size() / Math.sqrt(studyFeatures.size() * userFeatures.size());
    }
}
