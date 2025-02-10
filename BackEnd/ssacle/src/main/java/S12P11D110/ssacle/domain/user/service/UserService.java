package S12P11D110.ssacle.domain.user.service;

import S12P11D110.ssacle.domain.auth.repository.RefreshTokenRepository;
import S12P11D110.ssacle.domain.user.dto.request.SsafyAuthRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserNicknameRequest;
import S12P11D110.ssacle.domain.user.dto.request.UserProfileRequest;
import S12P11D110.ssacle.domain.user.dto.response.SsafyAuthResponse;
import S12P11D110.ssacle.domain.user.dto.response.UserProfileResponse;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.exception.ApiErrorException;
import S12P11D110.ssacle.global.exception.ApiErrorStatus;
import S12P11D110.ssacle.global.exception.AuthErrorException;
import S12P11D110.ssacle.global.exception.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

import static S12P11D110.ssacle.domain.user.entity.UserRole.SSAFYUSER;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

//------------------------------------------- << 로그아웃 & 탈퇴 >> -------------------------------------------
    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String userId, String accessToken) {
        // 해당 유저의 Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void delete(String userId) {
        // 해당 유저의 Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(userId);
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthErrorException(AuthErrorStatus.GET_USER_FAILED));
        // DB에서 유저 정보 삭제
        userRepository.delete(user);
    }


//------------------------------------------- << 프로필 >> -------------------------------------------
    /**
     * 싸피생 인증
     */
    @Transactional
    public SsafyAuthResponse ssafyAuth(String userId, @RequestBody SsafyAuthRequest request) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        // 싸피생 정보 설정
        // (일단 임시로 기수는 12기, 지역은 구미로 일괄 설정)
        user.setTerm("12기");
        user.setCampus("구미");
        user.setRole(SSAFYUSER);
        // DB 저장
        userRepository.save(user);
        return new SsafyAuthResponse("12기", "구미");
    }

    /**
     * 닉네임 변경 (증복 검사)
     */
    public String checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname) ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
    }
    public void updateNickname(String userId, UserNicknameRequest updateUserNicknameRequestDto) throws AuthErrorException {
        String newNickname = updateUserNicknameRequestDto.getUserNickname();
        if (userRepository.findByNickname(newNickname).isEmpty()) {
            Optional<User> optionalUser = userRepository.findById(userId);
            // ✅ User 객체가 존재하면 닉네임 변경 후 저장
            User user = optionalUser.orElseThrow(() -> new AuthErrorException(AuthErrorStatus.GET_USER_FAILED));
            user.setNickname(newNickname);
        } else {
            throw new ApiErrorException(ApiErrorStatus.DUPLICATED_USER_NAME);
        }
    }

    /**
     * (카카오 로그인 후) 프로필 생성
     */
    @Transactional
    public UserProfileResponse createUserProfile(String userId, UserProfileRequest request) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        // 닉네임 중복 검사 : 동시 요청 발생 가능성 때문에 다시 확인해야 함
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        // 프로필 입력 정보 받아오기 : 닉네임, 프로필 이미지, 관심 주제, 요일
        user.setNickname(request.getNickname());
        user.setImage(request.getImage());
        user.setTopics(request.getTopics());
        user.setMeetingDays(request.getMeetingDays());
        // DB에 프로필 저장
        User savedUser = userRepository.save(user);
        // 응답 DTO build
        UserProfileResponse response = UserProfileResponse.builder()
                .nickname(savedUser.getNickname())
                .image(savedUser.getImage())
                .term(savedUser.getTerm())
                .campus(savedUser.getCampus())
                .topics(savedUser.getTopics())
                .meetingDays(savedUser.getMeetingDays())
                .build();
        return response;
    }

    /**
     * 프로필 조회
     */
    public UserProfileResponse findUserProfile(String userId) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        // 응답 DTO build : 닉네임, 프로필 이미지, 기수, 캠퍼스, 관심 주제, 스터디 요일
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
    public UserProfileResponse modifyUserProfile(String userId, UserProfileRequest request) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));
        // 닉네임이 변경되었으면 중복 검사 : : 동시 요청 발생 가능성 때문에 다시 확인해야 함
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }
        // 프로필 이미지는 null 아닐 때 이미지 업로드하고, null이면 공백(안드로이드에 저장된 기본 이미지) 처리
        if (request.getImage() != null) {
            user.setImage(request.getImage());
        }
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


//------------------------------------------- << 스터디 >> -------------------------------------------
}
