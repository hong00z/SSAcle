package S12P11D110.ssacle.domain.feed.repository;

import S12P11D110.ssacle.domain.feed.entity.Feed;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends MongoRepository<Feed, String> {

    // feedId로 조회
//    Optional<Feed> findById(String id);

    // 스터디 에 해당하는 피드 조회
//    List<Feed> findByStudy(String id);

    // 피드 ID에 해당하는 모든 피드 리스트 반환;
//    List<String> findAllById(String id);

}
