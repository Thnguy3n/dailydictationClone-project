package com.example.userservice.repository;
import com.example.userservice.entity.UserChallengeProgressEntity;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserChallengeProgressRepository extends JpaRepository<UserChallengeProgressEntity,Long> {
    UserChallengeProgressEntity countAllByUserIdAndChallengeId(String userId, Long challengeId);
    Long countDistinctByUserIdAndIsCompletedAndChallengeIdIn(String userId, Integer isCompleted, Collection<Long> challengeIds);

    Long countDistinctChallengeIdByUserIdAndChallengeIdIn(String userId, Collection<Long> challengeId);

    UserChallengeProgressEntity findByChallengeIdAndUserId(Long challengeId, String userId);

    boolean existsByChallengeIdAndUserId(Long challengeId, String userId);

    Optional<UserChallengeProgressEntity> findFirstByUserIdAndChallengeIdInOrderByCompletedAtDesc(String userId, Collection<Long> challengeIds);

    Optional<UserChallengeProgressEntity> findFirstByUserIdAndChallengeIdInAndIsCompletedOrderByCompletedAtDesc(String userId, Collection<Long> challengeIds, Integer isCompleted);

    Optional<UserChallengeProgressEntity> findFirstByUserIdAndChallengeIdInOrderByLastAttemptAtDesc(String userId, Collection<Long> challengeIds);

    UserChallengeProgressEntity findFirstByUserIdAndLessonIdAndIsCompletedOrderByCompletedAtDesc(String userId, Long lessonId, Integer isCompleted);
}

