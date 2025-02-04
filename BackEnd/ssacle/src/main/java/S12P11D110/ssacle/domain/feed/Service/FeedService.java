package S12P11D110.ssacle.domain.feed.Service;

import S12P11D110.ssacle.domain.feed.dto.FeedCreateDTO;
import S12P11D110.ssacle.domain.feed.entity.Feed;
import S12P11D110.ssacle.domain.feed.repository.FeedRepository;
import S12P11D110.ssacle.domain.study.entity.Study;
import S12P11D110.ssacle.domain.study.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;
    private final StudyRepository studyRepository;


    // 피드 생성
    public void saveFeed (String studyId, String userId, FeedCreateDTO feedCreateDTO){
        Feed feed =  new Feed();

        feed.setTitle(feedCreateDTO.getTitle());
        feed.setContent(feedCreateDTO.getContent());
        feed.setStudy(studyId); // study: 스터디의 Id
        feed.setAuthor(userId); // author: 작성자의 Id

        // 피드 저장 및 저장된 피드의 Id 추출
        Feed savedFeed = feedRepository.save(feed);
        String feedId = savedFeed.getId();

        // 피드작성한 스터디의 feeds 필드에 feedId 추가
        Study study = studyRepository.findById(studyId)
                .orElseThrow(()-> new NoSuchElementException("스터디ID" + studyId + "에 해당하는 스터디가 없습니다."));
        Set<String> newFeed = new HashSet<>();
        newFeed.add(feedId);
        study.setFeeds(newFeed);

        // study 저장
        studyRepository.save(study);

    }



    // 피드 상세보기



}
