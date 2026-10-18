package me.kimyeonsup.home.domain.menu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AddMenuRequest (

    @NotNull
    Long categoryId,
    String emoji,
    @NotBlank
    String name,
    String description
) {}
