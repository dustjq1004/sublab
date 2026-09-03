package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum LoginExceptionMessage implements BaseExceptionMessage {

    REFRESH_TOKEN_NOT_FOUND("login.exception.refresh.token.not.found"),
    REFRESH_TOKEN_EXPIRED("login.exception.refresh.token.expired"),
    LOGIN_EMAIL_INVALID("login.exception.email.invalid"),
    LOGIN_USER_NOT_FOUND("login.exception.user.not.found");

    private final String messageKey;

    LoginExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
