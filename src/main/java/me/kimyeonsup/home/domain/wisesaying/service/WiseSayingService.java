package me.kimyeonsup.home.domain.wisesaying.service;

import me.kimyeonsup.home.domain.wisesaying.constants.WiseSaying;
import org.springframework.stereotype.Service;

@Service
public class WiseSayingService {

    public String getRandomWiseSaying() {
        return WiseSaying.getRandomWiseSaying().getSaying();
    }
}
