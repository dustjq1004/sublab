package me.kimyeonsup.home.util;

import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class CommonMarkUtil {

    private final static List<Extension> extensions = List.of(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create(),
            AutolinkExtension.create()
    );

    public static String renderHtml(String markdown) {
        Parser parser = Parser.builder()
                .extensions(extensions)
                .build();
        Node document = parser.parse(markdown);
        
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
        return renderer.render(document);
    }
}
