package me.kimyeonsup.home.domain.menu.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.kimyeonsup.home.global.common.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private List<Menu> menus = new ArrayList<>();

    @Builder
    public Category(String name, List<Menu> menus) {
        this.name = name;
        this.menus = menus != null ? menus : new ArrayList<>();
    }

    public void update(String name) {
        this.name = name;
    }

    public boolean hasMenus() {
        return !menus.isEmpty();
    }
}
