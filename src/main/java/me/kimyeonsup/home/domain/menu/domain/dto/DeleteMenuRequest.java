package me.kimyeonsup.home.domain.menu.domain.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMenuRequest {
    @Positive
    private long menuId;
}
