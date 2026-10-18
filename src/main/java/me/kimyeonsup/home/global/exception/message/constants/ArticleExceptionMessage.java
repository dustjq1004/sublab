package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum ArticleExceptionMessage implements BaseExceptionMessage {

    ARTICLE_NOT_FOUND("article.exception.not.found"),
    CREDENTIALS_MIS_MATCH("article.exception.credentials.mismatch"),
    ARTICLE_SAVE_FAILED("article.exception.save.failed");

    private String messageKey;

    ArticleExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
