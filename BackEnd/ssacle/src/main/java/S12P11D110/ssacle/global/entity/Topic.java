//package S12P11D110.ssacle.global.entity;
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Arrays;
//
//@RequiredArgsConstructor
//@Getter
//public enum Topic {
//    FE("프론트엔드"),
//    BE("백엔드"),
//    MOBILE("모바일"),
//    AI("인공지능"),
//    DATA("빅데이터"),
//    EMBEDDED("임베디드"),
//    INFRA("인프라"),
//    CS("CS이론"),
//    ALGORITHM("알고리즘"),
//    GAME("게임"),
//    EXTRA("기타");
//
//    private final String label;
//
//    // 한글 라벨 >>> Enum값을 찾는 정적 메서드
//    // gpt: from
//    public static Topic fromLabel(String label){  // 클라이언트가 보낸 한글
//        return Arrays.stream(Topic.values())
//                .filter(topic -> topic.getLabel().equals(label))
//                .findFirst()
//                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 주제입니다."+ label));
//    }
//    // gpt: to
//
//    // 영문 코드 >>>> 한글변환
//    // gpt: from
//    public static String toLable(String code){
//        return Arrays.stream(Topic.values())
//                .filter(topic -> topic.name().equals(code))
//                .map(Topic::getLabel) // 한글라벨로 반환
//                .findFirst()
//                .orElse(code); // 매칭이 안되면 원래 코드로 반환
//    }
//    // gpt: to
//
//
//
//
//
//
//
//
////    Topic(String label) {
////        this.label = label;
////    }
//
////    public String getLabel() {
////        return label;
////    }
//
//
//
//}