package com.example.audioservice.repository;

import com.example.audioservice.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface TopicRepository extends JpaRepository<TopicEntity, Long> {

    TopicEntity findByIdAndPremiumTopic(Long id, Integer premiumTopic);
}
