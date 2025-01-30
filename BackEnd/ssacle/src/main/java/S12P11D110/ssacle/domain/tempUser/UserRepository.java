package S12P11D110.ssacle.domain.tempUser;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User,String> {

    List<User> findAll();

    // findById(string id): ID로 유저 조회 조회
    Optional<User> findById(String id);
}
