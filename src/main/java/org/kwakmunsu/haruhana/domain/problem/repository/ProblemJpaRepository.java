package org.kwakmunsu.haruhana.domain.problem.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.kwakmunsu.haruhana.domain.problem.entity.Problem;
import org.kwakmunsu.haruhana.domain.problem.enums.ProblemDifficulty;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findFirstByCategoryTopicIdAndDifficultyAndStatusOrderByProblemAtDesc(
            Long categoryTopicId,
            ProblemDifficulty difficulty,
            EntityStatus status
    );

    // NOTE: LIMIT :limit OFFSET :offset는 표준 JPQL 문법이 아님.  "Hibernate 6.x 환경엔 작동함으로 의도적으로 사용"
    @Query("""
            SELECT p FROM Problem p
            JOIN FETCH p.categoryTopic
            WHERE p.problemAt = :date
              AND p.status = :status
            ORDER BY p.id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<Problem> findProblemsByProblemAt(
            @Param("date") LocalDate date,
            @Param("limit") long limit,
            @Param("offset") long offset,
            @Param("status") EntityStatus status
    );

    long countByProblemAtAndStatus(LocalDate today, EntityStatus status);
}