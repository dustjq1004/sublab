package me.kimyeonsup.home.domain.wisesaying.constants;

public enum WiseSaying {

    WISE_SAYING_1("인생은 짧고 예술은 길다."),
    WISE_SAYING_2("행복은 우리가 만드는 것이다."),
    WISE_SAYING_3("지혜는 경험에서 온다."),
    WISE_SAYING_4("오늘 할 수 있는 일을 내일로 미루지 마라."),
    WISE_SAYING_5("성공은 준비된 자에게 찾아온다."),
    WISE_SAYING_6("다른 사람의 행복보다 내가 가질 행복이 중요하다."),
    WISE_SAYING_7("세상에 나를 믿어줄 한 사람만 있으면 된다. 그러면 내가 무너져도 다시 일어설 수 있다."),
    WISE_SAYING_8("완벽한 사람은 없으니, 완벽한 하루도 없다."),
    WISE_SAYING_9("행복도 내가 만들고, 불행도 내가 만든다."),
    WISE_SAYING_10("당신의 인생의 주인공은 누구인가.");

    private final String saying;

    WiseSaying(String saying) {
        this.saying = saying;
    }

    public static WiseSaying getRandomWiseSaying() {
        return values()[(int) (Math.random() * values().length)];
    }

    public String getSaying() {
        return saying;
    }
}
