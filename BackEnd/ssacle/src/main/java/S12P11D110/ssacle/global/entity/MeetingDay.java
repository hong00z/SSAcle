package S12P11D110.ssacle.global.entity;

public enum MeetingDay {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금"),
    SAT("토"),
    SUN("일"),
    ;

    private final String label;

    MeetingDay(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
