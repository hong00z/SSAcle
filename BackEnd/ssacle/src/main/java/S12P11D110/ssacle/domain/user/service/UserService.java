package S12P11D110.ssacle.domain.user.service;

import S12P11D110.ssacle.domain.auth.repository.RefreshTokenRepository;
import S12P11D110.ssacle.domain.user.dto.request.SsafyAuthRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserProfileRequest;
import S12P11D110.ssacle.domain.user.dto.response.SsafyAuthResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserProfileResponse;
import S12P11D110.ssacle.domain.user.entity.Student;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.StudentRepository;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.exception.ApiErrorException;
import S12P11D110.ssacle.global.exception.ApiErrorStatus;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import S12P11D110.ssacle.global.service.FileStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static S12P11D110.ssacle.domain.user.entity.UserRole.SSAFYUSER;



@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentRepository studentRepository;
    // 프로필 이미지 관련
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final FileStorageServiceImpl fileStorageServiceImpl;

//------------------------------------------- << 로그아웃 & 탈퇴 >> -------------------------------------------
    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String userId, String refreshToken) {
        // 해당 유저의 Refresh Token 삭제
        refreshTokenRepository.deleteById(refreshToken);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUser(String userId, String refreshToken) {
        // 싸피생 인증 정보 삭제
        studentRepository.findByUserId(userId).ifPresent(student -> {
            student.setUserId(null);
            studentRepository.save(student);
        });
        // 해당 유저의 Refresh Token 삭제
        refreshTokenRepository.deleteById(refreshToken);
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(AuthErrorStatus.GET_USER_FAILED));
        // DB에서 유저 정보 삭제
        userRepository.delete(user);
    }


//------------------------------------------- << 프로필 >> -------------------------------------------
    /**
     * 프로필 조회
     */

//    String imageUrl = "http://43.203.250.200/images/" + uniqueFileName;

    public UserProfileResponse findUserProfile(String userId) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(AuthErrorStatus.GET_USER_FAILED));
        // 응답 DTO build : 닉네임, 프로필 사진, 기수, 캠퍼스, 관심 주제, 스터디 요일
        UserProfileResponse response = UserProfileResponse.builder()
                .nickname(user.getNickname())
                .image(user.getImage())
                .term(user.getTerm())
                .campus(user.getCampus())
                .topics(user.getTopics())
                .meetingDays(user.getMeetingDays())
                .build();
        return response;
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserProfileResponse modifyUserProfile(String userId, UserProfileRequest request, MultipartFile file) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 닉네임 변경이 됐다면 중복검사
        if(request.getNickname() != null && !request.getNickname().equals(user.getNickname())){
            if(userRepository.existsByNickname(request.getNickname())){
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            // 닉네임 변경
            user.setNickname(request.getNickname());
        }

        //  프로필 이미지 수정 후 저장
        try{
            // 파일 저장 경로 생성
            String uploadDir = fileStorageServiceImpl.getUploadDir(); // getUploadDir: 저장된 파일이 들어갈 디렉토리 경로를 반환하는 함수
            // 고유 파일명 생성
            logger.debug("Upload Directory: {}", uploadDir);

            String originalFileName = file.getOriginalFilename(); // 클라이언트에서 보낸 원본 파일명
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 확장자 추출
            String uniqueFileName = UUID.randomUUID().toString() + extension;// UUID를 붙여 고유한 파일명 생성


            //최종 파일 저장 경로
            Path filePath = Paths.get(uploadDir, uniqueFileName);
            logger.debug("Image saved at filePath={}", filePath);

            // 파일 저장
            Files.write(filePath, file.getBytes());
            logger.info("File saved successfully: {}", filePath.toAbsolutePath());

            // 저장된 파일명을 TempUser 엔티티에 반영
            user.setImage(uniqueFileName);


        }catch (IOException e){
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        // 스터디 관심 주제, 요일 수정
        user.setTopics(request.getTopics());
        user.setMeetingDays(request.getMeetingDays());
        userRepository.save(user);
        UserProfileResponse response = UserProfileResponse.builder()
                .nickname(user.getNickname())
                .image(user.getImage())
                .term(user.getTerm())
                .campus(user.getCampus())
                .topics(user.getTopics())
                .meetingDays(user.getMeetingDays())
                .build();
        return response;
    }

    /**
     * 닉네임 중복 검사
     */
    public boolean isNicknameDuplicated(String nickname, String currentUserNickname) {
        if (userRepository.existsByNickname(nickname)){
            // 현재 로그인한 사용자의 닉네임이면 중복으로 판단하지 않음
            if (nickname.equals(currentUserNickname)) {
                return true;    // '사용 가능한 닉네임입니다.'
            }
            return false;       // '이미 사용중인 닉네임입니다.'
        }
        return true;            // '사용 가능한 닉네임입니다.'
    }

    /**
     * 싸피생 인증
     */
    @Transactional
    public SsafyAuthResponse ssafyAuth(String userId, @RequestBody SsafyAuthRequest request) {
        // 1. 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        // 2. 학번으로 싸피생 정보 조회
        Student student = studentRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new ApiErrorException(ApiErrorStatus.INVALID_STUDENT_ID));
        // 3. 이름이랑 학번 일치하는지 확인
        if (!student.getName().equals(request.getName())) {
            throw new ApiErrorException(ApiErrorStatus.INVALID_STUDENT_INFO);
        }
        // 4. 이미 인증된 싸피생 정보인지 확인
        if (student.getUserId() != null && !student.getUserId().isEmpty()) {
            throw new ApiErrorException(ApiErrorStatus.ALREADY_AUTHENTICATED);
        }

        // users DB 저장
        user.setTerm(student.getTerm());
        user.setCampus(student.getCampus());
        user.setRole(SSAFYUSER);
        userRepository.save(user);
        // students DB 저장
        student.setUserId(userId);
        studentRepository.save(student);
        return new SsafyAuthResponse(student.getTerm(), student.getCampus());
    }


//------------------------------------------- << 스터디 >> -------------------------------------------
    /**
     * 개설한 스터디 등록 : 해당 유저의 createdStudies, joinedStudies에 studyId 추가
     */

    /**
     * 가입한 스터디 등록 : 해당 유저의 joinedStudies에 studyId 추가
     */

    /**
     * 가입한 스터디 조회 : 해당 유저의 joinedStudies 읽어오기
     */

    /**
     * 초대된 스터디 등록 : 해당 유저의 invitedStudies에 studyId 추가
     */

    /**
     * 초대된 스터디 조회 : 해당 유저의 invitedStudies 읽어오기
     */

    /**
     * 신청한 스터디 등록 : 해당 유저의 wishStudies에 studyId 추가
     */

    /**
     * 신청한 스터디 조회 : 해당 유저의 invitedStudies 읽어오기
     */

    /**
     * 신청한 스터디 삭제 : 해당 유저의 invitedStudies에 studyId 삭제
     */
}
