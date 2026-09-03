package me.kimyeonsup.home.domain.blog.admin.menu.repository;

import java.util.List;
import java.util.Optional;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.category WHERE m.category.id = :categoryId")
    List<Menu> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.category WHERE m.name = :name")
    Optional<Menu> findByName(@Param("name") String name);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.category")
    List<Menu> findAllWithCategory();
}
