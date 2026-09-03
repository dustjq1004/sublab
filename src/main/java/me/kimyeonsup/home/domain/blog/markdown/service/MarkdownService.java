package me.kimyeonsup.home.domain.blog.markdown.service;

import org.springframework.stereotype.Service;

@Service
public interface MarkdownService {
    String toHtml(String markdown);
    String toSafeHtml(String markdown);
}
