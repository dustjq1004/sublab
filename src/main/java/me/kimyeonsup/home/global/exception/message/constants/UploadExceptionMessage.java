package me.kimyeonsup.home.global.exception.message.constants;

import me.kimyeonsup.home.global.exception.message.BaseExceptionMessage;

public enum UploadExceptionMessage implements BaseExceptionMessage {

    UPLOAD_IMAGE_NOT_FOUND("upload.exception.image.not.found");


    private String messageKey;

    UploadExceptionMessage(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
