package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleBatchUpdateRequest {

    @Size(min = 1)
    private List<Long> articleIds;

    @NotBlank
    private String visibility;
}
