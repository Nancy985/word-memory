package com.wordmemory.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 艾宾浩斯间隔复习算法
 * 复习间隔：5分钟 → 30分钟 → 12小时 → 1天 → 2天 → 4天 → 7天 → 15天 → 30天
 */
public final class SpacedRepetitionUtil {

    private static final Duration[] REVIEW_INTERVALS = {
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            Duration.ofHours(12),
            Duration.ofDays(1),
            Duration.ofDays(2),
            Duration.ofDays(4),
            Duration.ofDays(7),
            Duration.ofDays(15),
            Duration.ofDays(30)
    };

    public static final int MAX_STAGE = REVIEW_INTERVALS.length;

    private SpacedRepetitionUtil() {
    }

    public static LocalDateTime calculateNextReview(int stage) {
        int index = Math.min(Math.max(stage, 0), REVIEW_INTERVALS.length - 1);
        return LocalDateTime.now().plus(REVIEW_INTERVALS[index]);
    }

    public static int advanceStage(int currentStage) {
        return Math.min(currentStage + 1, MAX_STAGE);
    }

    public static int resetStage() {
        return 0;
    }

    public static int calculateFamiliarity(int stage, int correctCount, int wrongCount) {
        int total = correctCount + wrongCount;
        if (total == 0) {
            return 0;
        }
        int base = (int) ((correctCount * 100.0) / total);
        int stageBonus = Math.min(stage * 5, 30);
        return Math.min(base + stageBonus, 100);
    }

    public static String getIntervalLabel(int stage) {
        int index = Math.min(Math.max(stage, 0), REVIEW_INTERVALS.length - 1);
        Duration d = REVIEW_INTERVALS[index];
        if (d.toDays() >= 1) {
            return d.toDays() + "天";
        }
        if (d.toHours() >= 1) {
            return d.toHours() + "小时";
        }
        return d.toMinutes() + "分钟";
    }

    public static String[] getAllIntervalLabels() {
        String[] labels = new String[REVIEW_INTERVALS.length];
        for (int i = 0; i < REVIEW_INTERVALS.length; i++) {
            labels[i] = getIntervalLabel(i);
        }
        return labels;
    }
}
