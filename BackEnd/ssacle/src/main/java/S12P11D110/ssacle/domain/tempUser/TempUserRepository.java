package S12P11D110.ssacle.domain.tempUser;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TempUserRepository extends MongoRepository<TempUser,String> {

    List<TempUser> findAll();

    // findById(string id): ID로 유저 조회 조회
    Optional<TempUser> findById(String id);

    Boolean existsByNickname(String id);


}
