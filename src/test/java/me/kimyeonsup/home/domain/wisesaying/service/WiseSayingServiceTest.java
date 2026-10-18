package me.kimyeonsup.home.domain.wisesaying.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WiseSayingServiceTest {

    @Autowired
    private WiseSayingService wiseSayingService;

    @Test
    @DisplayName("랜덤한 명언을 반환한다")
    void getRandomWiseSaying() {
        String randomWiseSaying = wiseSayingService.getRandomWiseSaying();

        assertNotNull(randomWiseSaying, "명언은 null이 아니어야 합니다.");
        assertFalse(randomWiseSaying.isEmpty(), "명언은 비어있지 않아야 합니다.");

        System.out.println("랜덤 명언: " + randomWiseSaying);
    }
}
