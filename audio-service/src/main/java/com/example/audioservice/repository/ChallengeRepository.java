package com.example.audioservice.repository;

import com.example.audioservice.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<ChallengeEntity, Long> {
    List<ChallengeEntity> findByLesson_Id(Long lessonId);

    List<ChallengeEntity> findAllByLesson_Id(Long lessonId);

    List<ChallengeEntity> findByLesson_IdOrderByOrderIndex(Long lessonId);


    Optional<ChallengeEntity> findByOrderIndexAndLesson_Id(Integer orderIndex, Long lessonId);

    ChallengeEntity findByLesson_IdAndOrderIndex(Long lessonId, Integer orderIndex);
}
