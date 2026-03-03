package org.kwakmunsu.haruhana.global.support.notification;

public interface ErrorNotificationSender {

    void sendErrorNotification(String message, Throwable throwable);

}