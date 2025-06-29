package com.example.audioservice.repository;

import com.example.audioservice.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<LessonEntity,Long> {
    List<LessonEntity> findAllBySectionEntity_Id(Long sectionEntityId);
}
