package me.kimyeonsup.home.domain.markdown.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.markdown.dto.MarkdownRenderReqeust;
import me.kimyeonsup.home.domain.markdown.dto.MarkdownRenderResponse;
import me.kimyeonsup.home.domain.markdown.service.MarkdownService;
import me.kimyeonsup.home.global.api.domain.dto.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/md")
@RequiredArgsConstructor
public class MarkdownController {

    private final MarkdownService markdownService;

    @PostMapping("/render")
    public ApiResponse<MarkdownRenderResponse> renderMarkdown(@RequestBody MarkdownRenderReqeust request) {
        log.debug("Rendering markdown content: {}", request.getContent());
        return ApiResponse.success(MarkdownRenderResponse.builder()
                        .content(markdownService.toSafeHtml(request.getContent()))
                        .build());
    }
}
