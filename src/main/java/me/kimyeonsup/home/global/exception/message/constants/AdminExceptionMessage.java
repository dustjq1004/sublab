package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum AdminExceptionMessage implements BaseExceptionMessage {

    ADMIN_BATCH_DELETE("admin.exception.batch.delete"),
    ADMIN_BATCH_MENU_UPDATE("admin.exception.batch.menu.update"),
    ADMIN_BATCH_VISIBILITY_UPDATE("admin.exception.batch.visibility.update");

    private String messageKey;

    AdminExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }
}
