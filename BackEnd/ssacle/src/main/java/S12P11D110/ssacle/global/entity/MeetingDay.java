//package S12P11D110.ssacle.global.entity;
//
//
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Getter
//public enum MeetingDay {
//    MON("월"),
//    TUE("화"),
//    WED("수"),
//    THU("목"),
//    FRI("금"),
//    SAT("토"),
//    SUN("일")
//    ;
//
//    private final String label;
//
//    // 한글 라벨 >>> Enum값을 찾는 정적 메서드
//    public static Set<MeetingDay> fromLabels(Set<String> labels){
//        return labels.stream()
//                .map(MeetingDay::fromLabel)
//                .collect(Collectors.toSet());
//    }
//
//    public static MeetingDay fromLabel (String label){
//        return Arrays.stream(MeetingDay.values())
//                .filter(meetingDay -> meetingDay.getLabel().equals(label))
//                .findFirst()
//                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 요일입니다: " + label));
//    }
//    // 영문 코드 >>>> 한글변환
//    public static String toLabels (Set<String> codes){
//        return codes.stream()
//                .map(MeetingDay::toLabel)
//                .collect(Collectors.toSet()).toString();
//    }
//
//    public static MeetingDay toLabel(String code){
//        return Arrays.stream(MeetingDay.values())
//                .filter(meetingDay -> meetingDay.name().equals(code))
//                .findFirst()
//                .orElse(code); // 매칭이 안되면 원래 코드 반환
//
//    }
//
//
//
////    MeetingDay(String label) {
////        this.label = label;
////    }
////
////    public String getLabel() {
////        return label;
////    }
//}