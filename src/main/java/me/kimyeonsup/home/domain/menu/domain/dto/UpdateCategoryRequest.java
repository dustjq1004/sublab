package me.kimyeonsup.home.domain.menu.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateCategoryRequest {
    @Positive
    private long id;

    @NotBlank
    private String name;
}
