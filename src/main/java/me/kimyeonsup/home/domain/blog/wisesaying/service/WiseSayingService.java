package me.kimyeonsup.home.domain.blog.wisesaying.service;

import me.kimyeonsup.home.domain.blog.wisesaying.constants.WiseSaying;
import org.springframework.stereotype.Service;

@Service
public class WiseSayingService {

    public String getRandomWiseSaying() {
        return WiseSaying.getRandomWiseSaying().getSaying();
    }
}
