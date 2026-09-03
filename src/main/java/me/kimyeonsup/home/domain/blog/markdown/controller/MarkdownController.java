package me.kimyeonsup.home.domain.blog.markdown.controller;

import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.domain.blog.markdown.dto.MarkdownRenderReqeust;
import me.kimyeonsup.home.domain.blog.markdown.dto.MarkdownRenderResponse;
import me.kimyeonsup.home.domain.blog.markdown.service.MarkdownService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/md")
@RequiredArgsConstructor
public class MarkdownController {

    private final MarkdownService markdownService;

    @PostMapping("/render")
    public ApiResponse<MarkdownRenderResponse> renderMarkdown(@RequestBody MarkdownRenderReqeust request) {
        return ApiResponse.success(MarkdownRenderResponse.builder()
                        .content(markdownService.toSafeHtml(request.getContent()))
                        .build());
    }
}
