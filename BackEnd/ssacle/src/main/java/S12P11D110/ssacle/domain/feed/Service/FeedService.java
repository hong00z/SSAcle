package S12P11D110.ssacle.domain.feed.Service;

import S12P11D110.ssacle.domain.feed.dto.request.FeedCreateDTO;
import S12P11D110.ssacle.domain.feed.entity.Feed;
import S12P11D110.ssacle.domain.feed.repository.FeedRepository;
import S12P11D110.ssacle.domain.study.entity.Study;
import S12P11D110.ssacle.domain.study.repository.StudyRepository;
import S12P11D110.ssacle.domain.user.entity.User;
import S12P11D110.ssacle.domain.user.repository.UserRepository;
import S12P11D110.ssacle.global.firebase.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final FirebaseMessagingService firebaseMessagingService;


    // 피드 생성
    public void saveFeed (String studyId, FeedCreateDTO feedCreateDTO){
        // 1. 피드 저장 (study에 feedId를 저장하지 않고, feed에 studyId를 저장한다.)
        Feed feed =  new Feed();
        feed.setTitle(feedCreateDTO.getTitle());
        feed.setContent(feedCreateDTO.getContent());
        feed.setStudy(studyId); // study: 스터디의 Id
        feedRepository.save(feed);

        // 2. FCM 토큰 보내기
        // 2-1. 스터디 찾기
        Study study = studyRepository.findById(studyId)
                .orElseThrow(()-> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));
        List<User> membersId = userRepository.findAllById(study.getMembers());
        System.out.println("멤버들의 토큰" + membersId);
        // 2-2. 스터디 멤버 찾기
        List<String> membersToken = membersId.stream()
                .map(User::getFcmToken)
                .toList();
        System.out.println("멤버들의 토큰" + membersToken);

        // 2-3. 스터디 멤버 별로 FCM 토큰 보내기
        for(String token : membersToken){
            System.out.println("멤버의 토큰" + token);
            // 토큰이 없으면 FCM 알림 안감
            if (!Objects.equals(token, "")){
                sendToMembers(token, study.getStudyName());
            }
        }


    }

    //  스터디원에게 FCM 메세지 작성
    public void sendToMembers (String token, String studyName ){
        String title = "피드 생성 알림";
        String body = studyName + "에 새로운 피드가 올라왔습니다!";

        firebaseMessagingService.sendNotification(token, title, body);
    }



    // 피드 상세보기

}
