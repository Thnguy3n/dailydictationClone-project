package com.example.audioservice.repository;

import com.example.audioservice.entity.SectionEntity;
import com.example.audioservice.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByTopicEntity_Id(Long topicEntityId);

    List<SectionEntity> findAllByTopicEntity(TopicEntity topicEntity);

    @Query("SELECT DISTINCT s FROM SectionEntity s " +
            "LEFT JOIN s.lessonEntities l " +
            "WHERE (:topicId IS NULL OR s.topicEntity.id = :topicId) " +
            "AND (:level IS NULL OR :level = '' OR s.level = :level) " +
            "AND (:lessonTitle IS NULL OR :lessonTitle = '' OR " +
            "LOWER(l.title) LIKE LOWER(CONCAT('%', :lessonTitle, '%')))")
    List<SectionEntity> findSectionsByFilter(
            @Param("topicId") Long topicId,
            @Param("level") String level,
            @Param("lessonTitle") String lessonTitle
    );
}
