package me.kimyeonsup.home.domain.menu.repository;

import java.util.List;
import me.kimyeonsup.home.domain.menu.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.menus")
    List<Category> findAllWithMenus();
}
