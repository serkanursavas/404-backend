package com.squad.squad.context;

public class GroupContext {

    private static final ThreadLocal<Integer> currentGroupId = new ThreadLocal<>();

    public static void setCurrentGroupId(Integer groupId) {
        currentGroupId.set(groupId);
    }

    public static Integer getCurrentGroupId() {
        return currentGroupId.get();
    }

    public static void clear() {
        currentGroupId.remove();
    }
}
