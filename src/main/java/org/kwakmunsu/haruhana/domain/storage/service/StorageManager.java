package org.kwakmunsu.haruhana.domain.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.member.entity.Member;
import org.kwakmunsu.haruhana.domain.storage.entity.Storage;
import org.kwakmunsu.haruhana.domain.storage.enums.UploadType;
import org.kwakmunsu.haruhana.domain.storage.repository.StorageJpaRepository;
import org.kwakmunsu.haruhana.global.support.image.StorageProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StorageManager {

    private final StorageProvider storageProvider;
    private final StorageReader storageReader;
    private final StorageJpaRepository storageJpaRepository;

    public void issue(Long memberId, UploadType uploadType, String objectKey) {
        storageJpaRepository.save(Storage.issue(memberId, uploadType, objectKey));
        log.info("[StorageManager] 업로드 발급 완료 - memberId={}, uploadType={}, objectKey={}", memberId, uploadType, objectKey);
    }

    // NOTE: 현재 로직은 프로필 이미지 업로드에 한정되어 있지만, 추후 확장 될 경우 업로드 타입에 따른 분기 처리가 필요할 수 있음. Storage는 확장성 고려하여 설계됨
    @Transactional
    public void completeUpload(String objectKey, Member member) {
        if (objectKey == null || member.hasMatchingObjectKey(objectKey)) {
            log.debug("[StorageManager] 업로드 완료 스킵 - 동일하거나 null인 objectKey, memberId={}", member.getId());
            return;
        }

        // 지금부턴 새로운 objectKey 일 경우
        Storage storage = storageReader.findByMemberIdAndObjectKey(member.getId(), objectKey);
        String oldKey = member.getProfileImageObjectKey();

        if (storage.isComplete()) {
            log.debug("[StorageManager] 업로드 완료 스킵 - 이미 완료된 objectKey={}", objectKey);
            return;
        }

        storageProvider.ensureObjectExists(objectKey);

        // 업로드 완료 처리 후 회원 프로필 이미지 정보 업데이트
        storage.complete(member.getId());
        member.updateProfileImageObjectKey(objectKey);
        log.info("[StorageManager] 프로필 이미지 업로드 완료 - memberId={}, objectKey={}", member.getId(), objectKey);

        // 이전 프로필 이미지 S3 객체 비동기 삭제
        storageProvider.deleteObjectAsync(oldKey);
    }

}