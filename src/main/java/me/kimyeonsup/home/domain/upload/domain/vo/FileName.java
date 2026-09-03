package me.kimyeonsup.home.domain.upload.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import me.kimyeonsup.home.global.exception.business.RequiredValueException;
import me.kimyeonsup.home.util.StringUtils;

@Embeddable
@Getter
public class FileName {

    @Column(name = "logical_name", nullable = false)
    String logicalName;   // 예: banner.png

    @Column(name = "physical_name")
    String physicalName;  // 예: 2025/10/13/uuid.png

    protected FileName() {}

    public FileName(String logicalName, String physicalName) {
        if (StringUtils.isNull(logicalName))
            throw RequiredValueException.forField("logicalName");

        this.logicalName = logicalName;
        this.physicalName = physicalName;
    }

    public String extension() {
        String name = (physicalName != null && !physicalName.isBlank())
                ? physicalName : logicalName;
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase();
    }
}
