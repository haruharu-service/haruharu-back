package org.kwakmunsu.haruhana.domain.member.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.dailyproblem.repository.DailyProblemJpaRepository;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.member.repository.MemberDeviceJpaRepository;
import org.kwakmunsu.haruhana.domain.member.repository.MemberJpaRepository;
import org.kwakmunsu.haruhana.domain.member.repository.MemberPreferenceJpaRepository;
import org.kwakmunsu.haruhana.domain.notification.repository.NotificationJpaRepository;
import org.kwakmunsu.haruhana.domain.storage.enums.UploadStatus;
import org.kwakmunsu.haruhana.domain.storage.repository.StorageJpaRepository;
import org.kwakmunsu.haruhana.domain.streak.repository.StreakJpaRepository;
import org.kwakmunsu.haruhana.domain.submission.repository.SubmissionJpaRepository;
import org.kwakmunsu.haruhana.global.entity.EntityStatus;
import org.kwakmunsu.haruhana.global.support.image.StorageProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@RequiredArgsConstructor
@Component
public class MemberRemover {

    private final MemberReader memberReader;
    private final MemberJpaRepository memberJpaRepository;
    private final StreakJpaRepository streakJpaRepository;
    private final MemberDeviceJpaRepository memberDeviceJpaRepository;
    private final SubmissionJpaRepository submissionJpaRepository;
    private final DailyProblemJpaRepository dailyProblemJpaRepository;
    private final MemberPreferenceJpaRepository memberPreferenceJpaRepository;
    private final StorageJpaRepository storageJpaRepository;
    private final NotificationJpaRepository notificationJpaRepository;
    private final StorageProvider storageProvider;

    @Transactional
    public void remove(Long memberId) {
        Member member = memberReader.find(memberId);
        LocalDateTime now = LocalDateTime.now();

        // 1. S3 삭제 대상 오브젝트 키 수집 (soft delete 전에 조회해야 함)
        List<String> s3ObjectKeysToDelete = storageJpaRepository.findObjectKeysByMemberId(memberId, UploadStatus.COMPLETED);

        // 2. 회원 엔티티 상태 변경 후 즉시 flush
        //    이후 bulk UPDATE 쿼리들이 영속성 컨텍스트를 초기화하기 때문에,
        //    member 변경 사항을 DB에 먼저 반영해야 한다.
        member.anonymize();  // nickname, loginId 익명화 (unique 충돌 방지)
        member.clearRefreshToken();
        member.delete();
        memberJpaRepository.saveAndFlush(member);

        // 3. 관련 도메인 데이터 일괄 soft delete (디바이스 정보는 Hard Delete)
        streakJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);
        memberDeviceJpaRepository.deleteAllByMemberId(memberId);
        submissionJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);
        dailyProblemJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);
        memberPreferenceJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);
        storageJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);
        notificationJpaRepository.softDeleteByMemberId(memberId, EntityStatus.DELETED, now);

        // 4. S3 삭제는 트랜잭션 커밋 후 실행
        //    트랜잭션 롤백 시 DB는 복구되지만 S3 삭제는 되돌릴 수 없으므로,
        //    커밋이 성공한 후에만 S3 오브젝트를 삭제한다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                s3ObjectKeysToDelete.forEach(key -> {
                    try {
                        storageProvider.deleteObjectAsync(key);
                    } catch (Exception e) {
                        log.warn("[MemberRemover] S3 오브젝트 삭제 실패 - memberId: {}, key: {}", memberId, key, e);
                    }
                });
            }
        });

        log.info("[MemberRemover] 회원 탈퇴 처리 완료 - memberId: {}", memberId);
    }

}
