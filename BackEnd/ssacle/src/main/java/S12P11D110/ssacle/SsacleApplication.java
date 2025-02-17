package S12P11D110.ssacle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SsacleApplication {
	public static final String[] Topics = {"웹 프론트", "백엔드", "모바일", "인공지능", "빅데이터", "임베디드", "인프라", "CS 이론", "알고리즘" , "게임", "기타"};
	public static final String[] MeetingDays = {"월", "화", "수" , "목", "금", "토", "일"};

	public static void main(String[] args) {
		SpringApplication.run(SsacleApplication.class, args);
	}

}
