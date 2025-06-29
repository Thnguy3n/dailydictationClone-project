package com.example.audioservice.repository;

import com.example.audioservice.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    CategoryEntity findAllById(Long id);

    boolean existsByTitle(String title);
}
