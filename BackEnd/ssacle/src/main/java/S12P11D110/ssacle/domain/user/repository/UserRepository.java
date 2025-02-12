package S12P11D110.ssacle.domain.user.repository;

import S12P11D110.ssacle.domain.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {

    List<User> findAll();

    // findById : ID로 사용자 조회
    Optional<User> findById(String id);

    // findByEmail : Email로 사용자 조회
    Optional<User> findByEmail(String email);

    // existsByNickname : 닉네임 중복 검사
    Boolean existsByNickname(String Nickname);

//    List<User> findALl
}
