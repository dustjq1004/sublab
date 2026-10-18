package me.kimyeonsup.home.infra.markdown.flexmark;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.markdown.service.MarkdownService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@Slf4j
@RequiredArgsConstructor
public class FlexmarkMarkdownRenderer implements MarkdownService {
    private final Parser parser;
    private final HtmlRenderer htmlRenderer;
    private final Safelist safelist;

    @Override
    public String toHtml(String markdown) {
        return htmlRenderer.render(parser.parse(markdown == null ? "" : markdown));
    }

    @Override
    public String toSafeHtml(String markdown) {
        String html = toHtml(markdown);
        log.debug("html: {}", html);
        String cleaned = Jsoup.clean(html, safelist);
        return cleaned.replaceAll("<input([^>]*?)>", "<input$1 disabled>");
    }
}
