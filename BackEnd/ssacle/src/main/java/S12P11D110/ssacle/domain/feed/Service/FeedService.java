package S12P11D110.ssacle.domain.feed.Service;

import S12P11D110.ssacle.domain.feed.dto.request.FeedCreateDTO;
import S12P11D110.ssacle.domain.feed.entity.Feed;
import S12P11D110.ssacle.domain.feed.repository.FeedRepository;
import S12P11D110.ssacle.domain.study.entity.Study;
import S12P11D110.ssacle.domain.study.repository.StudyRepository;
import S12P11D110.ssacle.domain.tempUser.TempUser;
import S12P11D110.ssacle.domain.tempUser.TempUserRepository;
import S12P11D110.ssacle.global.firebase.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final StudyRepository studyRepository;
    private final TempUserRepository tempUserRepository;
    private final FirebaseMessagingService firebaseMessagingService;


    // 피드 생성
    public void saveFeed (String studyId, String userId, FeedCreateDTO feedCreateDTO){
        Feed feed =  new Feed();

        feed.setTitle(feedCreateDTO.getTitle());
        feed.setContent(feedCreateDTO.getContent());
        feed.setStudy(studyId); // study: 스터디의 Id

        // 피드 저장 및 저장된 피드의 Id 추출
        Feed savedFeed = feedRepository.save(feed);
        String feedId = savedFeed.getId();

        // 피드작성한 스터디의 feeds 필드에 feedId 추가
//        Study study = studyRepository.findById(studyId)
//                .orElseThrow(()-> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));
//        Set<String> newFeed = new HashSet<>();
//        newFeed.add(feedId);
//        study.setFeeds(newFeed);

        // study 저장
//        studyRepository.save(study);

        // 스터디 찾기
        Study study = studyRepository.findById(studyId)
                .orElseThrow(()-> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));
        List<TempUser> membersId = tempUserRepository.findAllById(study.getMembers());
        System.out.println("멤버들의 토큰" + membersId);
        // 스터디 멤버 찾기
        List<String> membersToken = membersId.stream()
                .map(TempUser::getFcmToken)
                .toList();
        System.out.println("멤버들의 토큰" + membersToken);

        // 스터디 멤버 별로 토큰 보내기
        for(String token : membersToken){
            System.out.println("멤버의 토큰" + token);
            sendToMembers(token, study.getStudyName());
        }


    }

    // 스터디 팀장에게 FCM 던지기
    public void sendToMembers (String token, String studyName ){
        String title = "피드 생성 알림";
        String body = studyName + "에 새로운 피드가 올라왔습니다!";

        firebaseMessagingService.sendNotification(token, title, body);
    }
    // 피드 상세보기



}
