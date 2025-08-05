package com.squad.squad.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GroupContext {
    private static final ThreadLocal<Integer> currentGroupId = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(GroupContext.class);

    public static void setCurrentGroupId(Integer groupId) {
        currentGroupId.set(groupId);
        logger.debug("GroupId set to: {} for thread: {}", groupId, Thread.currentThread().getName());
    }

    public static Integer getCurrentGroupId() {
        Integer groupId = currentGroupId.get();
        if (groupId == null) {
            logger.warn("No groupId found in context for thread: {}", Thread.currentThread().getName());
        }
        return groupId;
    }

    // Onaylanmış grup ID'sini al - sadece APPROVED üyelikler için
    public static Integer getCurrentApprovedGroupId() {
        Integer groupId = currentGroupId.get();
        if (groupId == null || groupId <= 0) {
            // Null veya 0 ise onaylanmamış kullanıcı
            return null;
        }
        return groupId;
    }

    public static void clear() {
        currentGroupId.remove();
        logger.debug("GroupId cleared for thread: {}", Thread.currentThread().getName());
    }

    public static boolean hasGroupId() {
        return currentGroupId.get() != null;
    }

    public static void validateGroupId() {
        Integer groupId = getCurrentGroupId();
        if (groupId == null || groupId <= 0) {
            throw new IllegalStateException("Context'te geçerli groupId bulunamadı");
        }
    }

    // Current user ID için ThreadLocal
    private static final ThreadLocal<Integer> currentUserId = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        currentUserId.set(userId);
        logger.debug("UserId set to: {} for thread: {}", userId, Thread.currentThread().getName());
    }

    public static Integer getCurrentUserId() {
        Integer userId = currentUserId.get();
        if (userId == null) {
            logger.warn("No userId found in context for thread: {}", Thread.currentThread().getName());
        }
        return userId;
    }

    public static void clearCurrentUserId() {
        currentUserId.remove();
        logger.debug("UserId cleared for thread: {}", Thread.currentThread().getName());
    }
}