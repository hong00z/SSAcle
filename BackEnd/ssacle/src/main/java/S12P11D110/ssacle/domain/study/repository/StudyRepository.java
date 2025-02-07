package S12P11D110.ssacle.domain.study.repository;

import S12P11D110.ssacle.domain.study.entity.Study;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


// gpt: 36까지
@Repository
public interface StudyRepository extends MongoRepository<Study, String> {

    // findAll() : 모든 스터디 그룹 조회
    List<Study> findAll();

    // 해당 조건의 스터디 그룹 조회
    List<Study> findByTopicAndMeetingDaysIn(List<Study.Topic> topic, List<Study.MeetingDays>meetingDays);

    // findById(string id): ID로 스터디 그룹 조회
    Optional<Study> findById(String id);

    // userId로 스터디 조회
    List<Study> findByMembersContaining(String userId);

    // TempUser Id로 User가 가입한 스터디 그룹 조회

    // deleteById(String id): 스터디 삭제
    void deleteById (String id);
}
