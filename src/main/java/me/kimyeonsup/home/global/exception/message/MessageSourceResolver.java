package me.kimyeonsup.home.global.exception.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageSourceResolver {

    private final MessageSource messageSource;

    public String resolve(BaseExceptionMessage exceptionMessage) {
        return messageSource.getMessage(exceptionMessage.getMessageKey(), null, LocaleContextHolder.getLocale());
    }

    public String resolve(BaseExceptionMessage exceptionMessage, Object[] args) {
        return messageSource.getMessage(exceptionMessage.getMessageKey(), args, LocaleContextHolder.getLocale());
    }

    public String resolve(BaseExceptionMessage exceptionMessage, long id) {
        return resolve(exceptionMessage, new Object[]{id});
    }
}
