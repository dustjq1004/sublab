package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum DraftExceptionMessage implements BaseExceptionMessage {
    DRAFT_NOT_FOUND("exception.draft.not.found");

    private String messageKey;

    DraftExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
