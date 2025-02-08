package S12P11D110.ssacle.domain.tempUser;

import S12P11D110.ssacle.global.service.FileStorageService;
import S12P11D110.ssacle.global.service.FileStorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


//    private final UserProfileRequest userProfileRequest;
    private final UserRepository userRepository;
    private final FileStorageServiceImpl fileStorageServiceImpl;


    @Transactional
    public UserProfileResponse modifyUserProfile(String userId, UserProfileRequest request, MultipartFile file) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 닉네임 변경이 됐다면 중복검사
        if(request.getNickname() != null && !request.getNickname().equals(user.getNickname())){
            if(userRepository.existsByNickname(request.getNickname())){
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            // 닉네임 변경
            user.setNickname(request.getNickname());
        }
        //  프로필 이미지 저장
        try{
            // 파일 저장 경로 생성
            String uploadDir = fileStorageServiceImpl.getUploadDir(); // getUploadDir: 저장된 파일이 들어갈 디렉토리 경로를 반환하는 함수
            // 고유 파일명 생성
            String originalFileName = file.getOriginalFilename(); // 클라이언트에서 보낸 원본 파일명
            String extension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 확장자 추출
            String uniqueFileName = UUID.randomUUID().toString() + extension;// UUID를 붙여 고유한 파일명 생성

            //최종 파일 저장 경로
            Path filePath = Paths.get(uploadDir, uniqueFileName);

            // 파일 저장
            Files.write(filePath, file.getBytes());

            logger.debug("Image saved at filePath={}", filePath);

            // 저장된 파일명을 User 엔티티에 반영
            user.setImage(uniqueFileName);


        }catch (IOException e){
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }

        user.setTopics(request.getTopics());
        user.setMeetingDays(request.getMeetingDays());

        userRepository.save(user);

        UserProfileResponse response = UserProfileResponse.builder()
                .nickname(user.getNickname())
                .image(user.getImage())
                .topics(user.getTopics())
                .meetingDays(user.getMeetingDays())
                .build();

        return response;
    }



}
