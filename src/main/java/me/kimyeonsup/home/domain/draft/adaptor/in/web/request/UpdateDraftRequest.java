package me.kimyeonsup.home.domain.draft.adaptor.in.web.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDraftRequest {
    private Long version;
    private String title;
    private String subTitle;
    private String content;
    private Long menuId;
    private String thumbnailUrl;
    private String thumbnailLogicalFileName;
}
