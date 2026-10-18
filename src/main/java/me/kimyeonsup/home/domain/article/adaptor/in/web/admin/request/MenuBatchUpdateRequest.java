package me.kimyeonsup.home.domain.article.adaptor.in.web.admin.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuBatchUpdateRequest {

     @Size(min = 1)
     private List<Long> articleIds;

     @NotNull
     private Long targetMenuId;

     public List<Long> getArticleIds() {
          return articleIds;
     }
}
