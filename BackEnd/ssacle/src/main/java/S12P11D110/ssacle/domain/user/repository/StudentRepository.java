package S12P11D110.ssacle.domain.user.repository;

import S12P11D110.ssacle.domain.user.entity.Student;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StudentRepository extends MongoRepository<Student, String> {
    // 학번으로 싸피생 찾기
    Optional<Student> findByStudentId(int studentId);

    // userId로 싸피생 찾기 (회원 탈퇴 시 필요)
    Optional<Student> findByUserId(String userId);
}
