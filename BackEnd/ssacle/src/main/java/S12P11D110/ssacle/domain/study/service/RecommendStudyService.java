package S12P11D110.ssacle.domain.study.service;

import S12P11D110.ssacle.domain.study.dto.RecoStudyResult;
import S12P11D110.ssacle.domain.study.dto.response.RecommendStudyDTO;
import S12P11D110.ssacle.domain.study.dto.StudyDTO;
import S12P11D110.ssacle.domain.study.dto.UserConditionDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendStudyService {
    public List<RecoStudyResult> recommendStudy(UserConditionDTO userCondition, List<StudyDTO>allStudiesDTO){
        Set<String> userTopics = new HashSet<>(userCondition.getTopics());

        System.out.println("유저의 관심주제: "+ userTopics);
        System.out.println("스터디 전체: "+ allStudiesDTO);
        //1. 스터디 필터링
        List<StudyDTO> filteredStudies = allStudiesDTO.stream()
                .filter(study -> study.getCount()  > study.getMembers().size() ) // 정원 > 가입멤버수인 스터디
                .filter(study -> userTopics.contains(study.getTopic()))
                .filter(study -> { // member, wishMember preMember는 제외
                    // member, wishMember preMember객체 가져오기
                    Set<String> members = study.getMembers() ;
                    Set<String> wishMembers = study.getWishMembers() != null ? study.getWishMembers() : new HashSet<>();
                    Set<String> preMembers = study.getPreMembers() != null ? study.getPreMembers() : new HashSet<>();

                    boolean isAlreadyMembers = members.contains(userCondition.getUserId());
                    boolean isAlreadyWishMembers = wishMembers.contains(userCondition.getUserId());
                    boolean isAlreadyPreMembers = preMembers.contains(userCondition.getUserId());


                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 가입한 스터디인가?"+ isAlreadyMembers);
                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 요청받은 스터디인가?"+ isAlreadyWishMembers);
                    System.out.println("스터디ID"+ study.getStudyId()+", 이미 가입 신청한 스터디인가?"+ isAlreadyPreMembers);

                    return !(isAlreadyMembers||isAlreadyWishMembers||isAlreadyPreMembers);
                })
                .collect(Collectors.toList());

        System.out.println("필터링된 스터디 리스트" + filteredStudies);

        // 2. 코사인 유사도를 기반으로 스터디 추천

        Map<StudyDTO, Double> studySimilarityMap = new HashMap<>();
        for(StudyDTO study : filteredStudies){
            double similarity = calculateFiltering(userCondition, study);
            studySimilarityMap.put(study, similarity);

            System.out.println(study + ": " + similarity); // 디버깅
        }
        System.out.println("User Similarity Map: " + studySimilarityMap); // 디버깅

        //3. 유사도 순으로 내림차순 -> 상위 3개 스터디 추출
        return studySimilarityMap.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(3)
                .map(entry -> new RecoStudyResult(
                        entry.getKey().getStudyId(),
                        entry.getValue(), // 유사도
                        entry.getKey().getStudyName(),
                        entry.getKey().getTopic(),
                        entry.getKey().getMeetingDays(),
                        entry.getKey().getCount(),
                        entry.getKey().getMembers(),
                        entry.getKey().getCreatedBy()
                ))
                .collect(Collectors.toList());
    }

    // 코사인 유사도 계산
    public double calculateFiltering(UserConditionDTO userCondition , StudyDTO study) {
        // 1. 유저와 스터디의 주제 및 모임 요일 백터화
        Set<String> userFeatures = new HashSet<>();
        userFeatures.addAll(userCondition.getTopics());
        userFeatures.addAll(userCondition.getMeetingDays());

        Set<String> studyFeatures = new HashSet<>();
        studyFeatures.add(study.getTopic());
        studyFeatures.addAll(study.getMeetingDays());

        // 2. 교집합 및 합집합 크기를 계산
        Set<String> intersection = new HashSet<>(userFeatures);
        intersection.retainAll(studyFeatures); // 교집합

        Set<String> union = new HashSet<>(userFeatures);
        union.addAll(studyFeatures); // 합집합

        // 3. 코사인 유사도 계산
        if (union.isEmpty()) return 0.0;
        double cosineResult = intersection.size() / Math.sqrt(studyFeatures.size() * userFeatures.size());

        double filterResult = calculateFilterResult(userCondition, study);
        return (cosineResult + filterResult) / 2;
    }

    private double calculateFilterResult(UserConditionDTO userCondition, StudyDTO study) {

        // 1. user 의 topic 개수, meetingDay 개수
        int topicsCount = userCondition.getTopics().size();
        int meetingDaysCount = userCondition.getMeetingDays().size();

        // 2. eachOfTopic / eachOfMeetingDay 값 구하기
        double  eachOfTopic = 0.5/ topicsCount;
        double  eachOfMeetingDays = 0.5/meetingDaysCount;

        // 3.겹치는 topic 안겹치는 topic 겹치는 meetingday 안겹치는 meetingday 개수 구하기
        Set<String> userTopics = new HashSet<>(userCondition.getTopics());
        Set<String> studyTopics = new HashSet<>(Set.of(study.getTopic()));

        Set<String> userMeetingDays = new HashSet<>(userCondition.getMeetingDays());
        Set<String> studyMeetingDays = new HashSet<>(study.getMeetingDays());
        // topic 의 교집합 구하기
        Set<String> topicIntersection = new HashSet<>(userTopics);
        topicIntersection.retainAll(studyTopics);
        //meetingDay 의 교집합 구하기
        Set<String> meetingDaysIntersection = new HashSet<>(userMeetingDays);
        meetingDaysIntersection.retainAll(studyMeetingDays);

        int sameTopics = topicIntersection.size();
        int diffTopics = topicsCount - sameTopics;
        int sameMeetingDays = meetingDaysIntersection.size();
        int diffMeetingDays = meetingDaysCount - sameMeetingDays;

        // 4. 점수 더하기고 평균 내기
        return  (topicsCount * eachOfTopic - (diffTopics * eachOfTopic + 0.001))
                +(meetingDaysCount * eachOfMeetingDays -(diffMeetingDays * 0.001));

    }
}
