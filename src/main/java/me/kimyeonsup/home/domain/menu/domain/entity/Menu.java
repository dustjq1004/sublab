package me.kimyeonsup.home.domain.menu.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.domain.article.domain.model.Article;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "emoji")
    private String emoji;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<Article> articles = new ArrayList<>();

    @Builder
    public Menu(Long id, String name, String description, String emoji, Category category, List<Article> articles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.category = category;
        this.articles = articles != null ? articles : new ArrayList<>();
    }


    public void update(String name, String description, String emoji, Category category) {
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.category = category;
    }

    public boolean hasArticles() {
        return !this.articles.isEmpty();
    }
}
