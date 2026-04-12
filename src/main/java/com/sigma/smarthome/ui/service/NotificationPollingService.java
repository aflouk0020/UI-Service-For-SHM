package com.sigma.smarthome.ui.service;

import com.sigma.smarthome.ui.model.NotificationItem;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class NotificationPollingService {

    private static final int PAGE_SIZE = 10;

    private final NotificationApiService notificationApiService = new NotificationApiService();
    private final Set<String> knownNotificationIds = new HashSet<>();

    private ScheduledService<List<NotificationItem>> scheduledService;
    private Consumer<NotificationItem> onNewNotification;
    private Consumer<Integer> onUnreadCountChanged;

    private boolean initialLoadCompleted = false;

    public void setOnNewNotification(Consumer<NotificationItem> onNewNotification) {
        this.onNewNotification = onNewNotification;
    }

    public void setOnUnreadCountChanged(Consumer<Integer> onUnreadCountChanged) {
        this.onUnreadCountChanged = onUnreadCountChanged;
    }

    public void start() {
        stop();

        scheduledService = new ScheduledService<>() {
            @Override
            protected Task<List<NotificationItem>> createTask() {
                return new Task<>() {
                    @Override
                    protected List<NotificationItem> call() {
                        return notificationApiService.getNotifications(0, PAGE_SIZE);
                    }
                };
            }
        };

        scheduledService.setDelay(Duration.seconds(1));
        scheduledService.setPeriod(Duration.seconds(5));

        scheduledService.setOnSucceeded(event -> {
            List<NotificationItem> notifications = scheduledService.getValue();

            if (notifications == null) {
                return;
            }

            int unreadCount = (int) notifications.stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getIsRead()))
                    .count();

            if (onUnreadCountChanged != null) {
                Platform.runLater(() -> onUnreadCountChanged.accept(unreadCount));
            }

            if (!initialLoadCompleted) {
                for (NotificationItem item : notifications) {
                    if (item.getId() != null && !item.getId().isBlank()) {
                        knownNotificationIds.add(item.getId());
                    }
                }
                initialLoadCompleted = true;
                return;
            }

            for (int i = notifications.size() - 1; i >= 0; i--) {
                NotificationItem item = notifications.get(i);

                if (item.getId() == null || item.getId().isBlank()) {
                    continue;
                }

                if (!knownNotificationIds.contains(item.getId())) {
                    knownNotificationIds.add(item.getId());

                    if (onNewNotification != null) {
                        Platform.runLater(() -> onNewNotification.accept(item));
                    }
                }
            }
        });

        scheduledService.setOnFailed(event -> {
            Throwable ex = scheduledService.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        scheduledService.start();
    }

    public void stop() {
        if (scheduledService != null) {
            scheduledService.cancel();
            scheduledService = null;
        }
    }

    public void reset() {
        knownNotificationIds.clear();
        initialLoadCompleted = false;
    }
}