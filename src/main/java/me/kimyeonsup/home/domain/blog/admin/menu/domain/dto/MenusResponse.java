package me.kimyeonsup.home.domain.blog.admin.menu.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MenusResponse {

    List<MenuResponse> menus;

    @Builder
    public MenusResponse(List<MenuResponse> menus) {
        this.menus = menus;
    }
}
