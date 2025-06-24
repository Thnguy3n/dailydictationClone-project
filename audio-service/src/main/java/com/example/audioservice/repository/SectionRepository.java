package com.example.audioservice.repository;

import com.example.audioservice.entity.SectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    List<SectionEntity> findAllByTopicEntity_Id(Long topicEntityId);
}
