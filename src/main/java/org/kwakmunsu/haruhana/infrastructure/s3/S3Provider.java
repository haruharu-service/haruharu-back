package org.kwakmunsu.haruhana.infrastructure.s3;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.storage.enums.FileContentType;
import org.kwakmunsu.haruhana.domain.storage.enums.UploadType;
import org.kwakmunsu.haruhana.global.support.error.ErrorType;
import org.kwakmunsu.haruhana.global.support.error.HaruHanaException;
import org.kwakmunsu.haruhana.global.support.image.StorageProvider;
import org.kwakmunsu.haruhana.infrastructure.s3.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Provider implements StorageProvider {

    private static final int DEFAULT_UPLOAD_EXPIRATION_MINUTES = 3;
    private static final int DEFAULT_READ_EXPIRATION_HOURS = 1;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public PresignedUrlResponse generatePresignedUploadUrl(
            UploadType uploadType,
            FileContentType fileContentType
    ) {
        try {
            String key = generateS3Key(uploadType, fileContentType);

            // Presigned 요청 구성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(DEFAULT_UPLOAD_EXPIRATION_MINUTES))
                    .putObjectRequest(p -> {
                        p.bucket(bucket);
                        p.key(key);
                        p.contentType(fileContentType.getMimeType());
                    })
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("[S3Provider] Upload Presigned URL 생성 완료 - objectKey: {}", key);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .objectKey(key)
                    .build();

        } catch (Exception e) {
            log.error("[S3Provider] Upload Presigned URL 생성 실패", e);
            throw new HaruHanaException(ErrorType.S3_PRESIGNED_URL_ERROR);
        }
    }

    @Override
    public String generatePresignedReadUrl(String objectKey) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(DEFAULT_READ_EXPIRATION_HOURS))
                    .getObjectRequest(g -> {
                        g.bucket(bucket);
                        g.key(objectKey);
                    })
                    .build();
            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);

            String presignedReadUrl = presignedGetObjectRequest.url().toString();

            log.info("[S3Provider] Read Presigned URL 생성 완료 - objectKey: {}", objectKey);

            return presignedReadUrl;
        } catch (Exception e) {
            log.error("[S3Provider] Get Presigned URL 생성 실패", e);
            throw new HaruHanaException(ErrorType.S3_PRESIGNED_URL_ERROR);
        }
    }

    @Override
    public void ensureObjectExists(String objectKey) {
        try {
            s3Client.headObject(r -> r
                    .bucket(bucket)
                    .key(objectKey)
            );
            log.debug("[S3Provider] S3 객체 존재 확인 완료 - objectKey: {}", objectKey);
        } catch (S3Exception e) {
            log.error("[S3Provider] S3 객체 존재 확인 실패 - objectKey: {}", objectKey, e);
            throw new HaruHanaException(ErrorType.NOT_FOUND_FILE);
        } catch (Exception e) {
            log.error("[S3Provider] 서버 내부 오류로 인한 S3 객체 존재 확인 실패 - objectKey: {}", objectKey, e);
            throw new HaruHanaException(ErrorType.NOT_FOUND_FILE);
        }
    }

    @Async
    @Override
    public void deleteObjectAsync(String oldKey) {
        if (oldKey == null || oldKey.isBlank()) {
            log.debug("[S3Provider] S3 객체 삭제 스킵 - 빈 objectKey");
            return;
        }

        try {
            s3Client.deleteObject(r -> r
                    .bucket(bucket)
                    .key(oldKey)
            );
            log.info("[S3Provider] S3 객체 삭제 완료 - objectKey: {}", oldKey);
        } catch (Exception e) {
            log.error("[S3Provider] S3 객체 삭제 실패 - objectKey: {}", oldKey, e);
        }
    }

    /**
     * S3 objectKey 생성: uploads/{yyyy-mm-dd}/{uploadType}/{UUID}.{extension}
     */
    private String generateS3Key(UploadType uploadType, FileContentType fileContentType) {
        String uuid = UUID.randomUUID().toString().substring(0, 16);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        String key = "uploads/%s/%s/%s.%s".formatted(
                today,
                uploadType.name().toLowerCase(),
                uuid,
                fileContentType.getExtension()
        );

        log.debug("[S3Provider] S3 Key 생성 - uploadType: {}, contentType: {}, key: {}",
                uploadType.name(), fileContentType.getMimeType(), key);

        return key;
    }

}