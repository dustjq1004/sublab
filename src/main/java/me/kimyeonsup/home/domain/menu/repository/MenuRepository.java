package me.kimyeonsup.home.domain.menu.repository;

import java.util.List;
import me.kimyeonsup.home.domain.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.category WHERE m.category.id = :categoryId")
    List<Menu> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.category")
    List<Menu> findAllWithCategory();
}
