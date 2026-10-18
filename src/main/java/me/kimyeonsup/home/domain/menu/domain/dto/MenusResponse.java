package me.kimyeonsup.home.domain.menu.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MenusResponse {

    private final List<MenuResponse> menus;

    @Builder
    public MenusResponse(List<MenuResponse> menus) {
        this.menus = menus;
    }
}
