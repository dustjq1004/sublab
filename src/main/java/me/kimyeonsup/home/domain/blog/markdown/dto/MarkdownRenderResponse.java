package me.kimyeonsup.home.domain.blog.markdown.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarkdownRenderResponse {
    private String content;
}
