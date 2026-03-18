package org.kwakmunsu.haruhana.infrastructure.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.haruhana.domain.notification.enums.NotificationType;
import org.kwakmunsu.haruhana.global.support.notification.ErrorNotificationSender;
import org.kwakmunsu.haruhana.global.support.notification.NotificationSender;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;
    private final ErrorNotificationSender errorNotificationSender;

    @Override
    public void sendNotification(String fcmToken, String title, String body, NotificationType type) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putData("type", type.name())
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("[FcmNotificationSender] 알림 발송 성공. messageId: {}, type: {}", response, type);
        } catch (FirebaseMessagingException e) {
            log.error("[FcmNotificationSender] 알림 발송 실패. type: {}", type, e);
            errorNotificationSender.sendErrorNotification("[FcmNotificationSender] 알림 발송 실패. type: " + type + ", error: " + e.getMessage(), e);
        }
    }

}