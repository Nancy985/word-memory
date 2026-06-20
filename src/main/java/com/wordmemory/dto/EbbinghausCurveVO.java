package com.wordmemory.dto;

import java.util.List;

public class EbbinghausCurveVO {

    private List<StageInfo> stages;
    private List<StageCount> userDistribution;

    public List<StageInfo> getStages() {
        return stages;
    }

    public void setStages(List<StageInfo> stages) {
        this.stages = stages;
    }

    public List<StageCount> getUserDistribution() {
        return userDistribution;
    }

    public void setUserDistribution(List<StageCount> userDistribution) {
        this.userDistribution = userDistribution;
    }

    public static class StageInfo {
        private int stage;
        private String interval;
        private String description;

        public StageInfo() {
        }

        public StageInfo(int stage, String interval, String description) {
            this.stage = stage;
            this.interval = interval;
            this.description = description;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class StageCount {
        private int stage;
        private long count;

        public StageCount() {
        }

        public StageCount(int stage, long count) {
            this.stage = stage;
            this.count = count;
        }

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
