package me.kimyeonsup.home.domain.markdown.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarkdownRenderResponse {
    private String content;
}
