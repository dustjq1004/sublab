package me.kimyeonsup.home.infra.markdown.flexmark;


import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.resizable.image.ResizableImageExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.List;
import me.kimyeonsup.home.domain.blog.markdown.service.MarkdownService;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlexmarkConfig {

    @Bean
    public Parser flexmarkParser() {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, List.of(
                        TablesExtension.create(),
                        TaskListExtension.create(),
                        StrikethroughExtension.create(),
                        AutolinkExtension.create(),
                        DefinitionExtension.create(),
                        TocExtension.create(),
                        YamlFrontMatterExtension.create(),
                        AnchorLinkExtension.create(),
                        AsideExtension.create(),
                        InsExtension.create(),
                        SuperscriptExtension.create(),
                        TypographicExtension.create(),
                        AbbreviationExtension.create(),
                        EmojiExtension.create(),
                        ResizableImageExtension.create(),
                        JekyllTagExtension.create(),
                        YouTubeLinkExtension.create(),
                        AdmonitionExtension.create()
                ))
                .set(HtmlRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX, "language-")
                .set(TocExtension.TITLE, "목차")
                .set(TocExtension.DIV_CLASS, "toc")
                .set(TocExtension.LIST_CLASS, "toc-list");

        return Parser.builder(options).build();
    }

    @Bean
    public HtmlRenderer flexmarkHtmlRenderer(Parser parser) {
        return HtmlRenderer.builder(parser.getOptions()).build();
    }

    @Bean
    public Safelist markdownSafelist() {
        return Safelist.relaxed()
                .preserveRelativeLinks(true)
                .addTags("table","thead","tbody","tr","th","td", "path","blockquote","code","pre","ul","ol","li","use"
                        ,"del","input","aside","svg","div","a","span","p","img","symbol","h1","h2","h3","h4","h5","h6"
                        ,"g","br","hr","strong","em","b","i","small","mark","sub","sup")
                .addAttributes(":all", "style", "class", "id", "data-*")
                .addAttributes("a", "class", "id", "style", "href", "target")
                .addAttributes("img", "class", "id", "style", "src", "alt")
                .addAttributes("svg","path","class", "id", "style", "width", "height", "viewBox", "xmlns", "fill")
                .addAttributes("path", "d", "fill", "stroke", "stroke-width"
                        , "stroke-linecap", "stroke-linejoin", "transform")
                .addAttributes("use", "xlink:href")
                .addAttributes("g", "fill")
                .addAttributes("input", "type", "checked", "disabled")
                .addAttributes("a", "href", "title", "class", "aria-hidden")
                .addProtocols("a", "href", "#", "http", "https", "mailto")
                .addProtocols("href","http","https","mailto");
    }

    @Bean
    public MarkdownService  markdownService(Parser parser, HtmlRenderer renderer, Safelist safelist) {
        return new FlexmarkMarkdownRenderer(parser, renderer, safelist);
    }
}
