package me.kimyeonsup.home.domain.menu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateMenuRequest {
    @Positive
    private long id;

    @NotNull
    @Positive
    private Long categoryId;

    private String emoji;

    @NotBlank
    private String name;

    private String description;
}
