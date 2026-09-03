package me.kimyeonsup.home.domain.upload.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import me.kimyeonsup.home.global.exception.business.InvalidFileSizeException;

@Embeddable
@Getter
public class FileSize {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB

    @Column(name = "size", nullable = false)
    private long size;

    protected FileSize() {
    }

    public FileSize(long size) {
        if (size < 0) {
            throw new InvalidFileSizeException("파일 크기는 0 이상이어야 합니다.");
        }

        if (size > MAX_SIZE) {
            throw new InvalidFileSizeException(
                String.format("파일 크기는 5MB를 초과할 수 없습니다. (현재: %.2fMB)", 
                    size / 1024.0 / 1024.0)
            );
        }

        this.size = size;
    }
}
