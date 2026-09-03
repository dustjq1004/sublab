package me.kimyeonsup.home.domain.blog.draft.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import me.kimyeonsup.home.domain.blog.article.domain.vo.Thumbnail;
import me.kimyeonsup.home.domain.blog.draft.domain.entity.DraftArticle;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDraftArticle {

    private Long id;
    private Long version;
    private String title;
    private String subTitle;
    private String content;
    private Long menuId;
    private String thumbnailUrl;
    private String thumbnailLogicalFileName;

    public DraftArticle toEntity(String author) {
        Thumbnail thumbnail = (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty())
                ? Thumbnail.of(thumbnailUrl, thumbnailLogicalFileName)
                : Thumbnail.empty();
        return DraftArticle.builder()
                .title(title)
                .content(content)
                .subTitle(subTitle)
                .menu(Menu.builder().id(menuId).build())
                .author(author)
                .thumbnailUrl(thumbnail)
                .build();
    }
}
