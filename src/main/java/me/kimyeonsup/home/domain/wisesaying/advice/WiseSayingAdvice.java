package me.kimyeonsup.home.domain.wisesaying.advice;

import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.wisesaying.constants.WiseSaying;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Slf4j
@Component
@Aspect
public class WiseSayingAdvice {

    //@Around("execution(* me.kimyeonsup.home.domain.article.controller.*ViewController.*(..))")
    public Object addWiseSayingToModel(ProceedingJoinPoint joinPoint) throws Throwable {
        Model model = null;

        // 컨트롤러 메서드 실행 전
        log.debug("Before Controller execution");

        // 컨트롤러 메서드 실행
        Object result = joinPoint.proceed();

        // 컨트롤러 메서드 실행 후
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Model) {
                model = (Model) arg;
                break;
            }
        }

        if (model != null) {
            WiseSaying randomWiseSaying = WiseSaying.getRandomWiseSaying();
            log.debug("WiseSaying : {}", randomWiseSaying.getSaying());
            model.addAttribute("wiseSaying", randomWiseSaying.getSaying());
        }

        log.debug("After Controller execution");
        return result;
    }
}
