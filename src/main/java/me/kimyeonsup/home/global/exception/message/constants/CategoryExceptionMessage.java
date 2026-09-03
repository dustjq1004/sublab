package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum CategoryExceptionMessage implements BaseExceptionMessage {
    CATEGORY_NOT_FOUND("category.exception.not.found"),
    CATEGORY_HAS_MENUS("category.exception.delete.has.menus");

    private String messageKey;

    CategoryExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
