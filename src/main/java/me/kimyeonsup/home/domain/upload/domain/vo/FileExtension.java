package me.kimyeonsup.home.domain.upload.domain.vo;

import java.util.Arrays;
import me.kimyeonsup.home.global.exception.business.InvalidFileFormatException;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import me.kimyeonsup.home.util.StringUtils;

public enum FileExtension {
    JPG("jpg", FileType.IMAGE),
    JPEG("jpeg", FileType.IMAGE),
    PNG("png", FileType.IMAGE),
    GIF("gif", FileType.IMAGE),
    WEBP("webp", FileType.IMAGE);

    private final String value;
    private final FileType type;

    FileExtension(String value, FileType type) {
        this.value = value;
        this.type = type;
    }

    public String getFileExtension() {
        return value;
    }

    public static FileExtension from(String ext, FileType validType) {
        if (StringUtils.isNull(ext)) {
            throw new RequiredValueException("ext");
        }
        return Arrays.stream(values())
                .filter(e -> e.type == validType)
                .filter(e -> e.value.equalsIgnoreCase(ext))
                .findFirst()
                .orElseThrow(() -> new InvalidFileFormatException("허용되지 않는 확장자입니다. 허용 확장자 - jpg, jpeg, png, gif, webp"));
    }
}
