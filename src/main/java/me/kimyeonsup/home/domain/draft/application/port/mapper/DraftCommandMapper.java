package me.kimyeonsup.home.domain.draft.application.port.mapper;

import me.kimyeonsup.home.domain.article.domain.model.Thumbnail;
import me.kimyeonsup.home.domain.draft.application.port.in.command.CreateDraftCommand;
import me.kimyeonsup.home.domain.draft.application.port.in.result.DraftResult;
import me.kimyeonsup.home.domain.draft.domain.model.DraftArticle;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class DraftCommandMapper {

    public DraftArticle toEntity(CreateDraftCommand command) {
        return DraftArticle.builder()
                .title(command.title())
                .subTitle(command.subTitle())
                .content(command.content())
                .author(command.author())
                .menu(Menu.builder().id(command.menuId()).build())
                .thumbnailUrl(toThumbnail(command.thumbnailUrl(), command.thumbnailLogicalFileName()))
                .build();
    }

    public DraftResult toResult(DraftArticle draft) {
        return DraftResult.builder()
                .id(draft.getId())
                .version(draft.getVersion())
                .title(draft.getTitle())
                .subTitle(draft.getSubTitle())
                .content(draft.getContent())
                .author(draft.getAuthor())
                .menuId(draft.getMenu().getId())
                .thumbnailUrl(draft.getThumbnailUrl())
                .thumbnailLogicalFileName(draft.getThumbnailLogicalFileName())
                .build();
    }

    public Thumbnail toThumbnail(String url, String logicalFileName) {
        return url == null || url.isBlank() ? Thumbnail.empty() : Thumbnail.of(url, logicalFileName);
    }
}
