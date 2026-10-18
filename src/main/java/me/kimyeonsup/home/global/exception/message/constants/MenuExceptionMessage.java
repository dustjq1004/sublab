package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum MenuExceptionMessage implements BaseExceptionMessage {

    MENU_NOT_FOUND("menu.exception.not.found"),
    MENU_DELETE_HAS_CHILD("menu.exception.delete.has.child"),
    MENU_DELETE_HAS_ARTICLE("menu.exception.delete.has.article"),
    MENU_DELETE_HAS_DRAFT("menu.exception.delete.has.draft");


    private String messageKey;

    MenuExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
