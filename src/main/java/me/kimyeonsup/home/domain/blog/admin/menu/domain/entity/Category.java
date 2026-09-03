package me.kimyeonsup.home.domain.blog.admin.menu.domain.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

@Entity
@Getter
@RequiredArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "emoji")
    private String emoji;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Menu> menus;

    @Builder
    public Category(String name, List<Menu> menus) {
        this.name = name;
        this.menus = menus;
    }

    public void update(String name) {
        this.name = name;
    }

    public boolean hasMenus() {
        return menus.size() > 0;
    }
}
