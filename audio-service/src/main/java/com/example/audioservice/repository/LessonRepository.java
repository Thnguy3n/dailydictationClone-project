package com.example.audioservice.repository;

import com.example.audioservice.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<LessonEntity,Long> {
}
