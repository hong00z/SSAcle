package S12P11D110.ssacle.global.entity;

public enum Topic {
    FE("프론트엔드"),
    BE("백엔드"),
    MOBILE("모바일"),
    AI("인공지능"),
    DATA("빅데이터"),
    EMBEDDED("임베디드"),
    INFRA("인프라"),
    CS("CS이론"),
    ALGORITHM("알고리즘"),
    GAME("게임"),
    EXTRA("기타");

    private final String label;

    Topic(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
